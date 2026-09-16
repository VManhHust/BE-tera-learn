package vn.tera.learn.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import vn.tera.learn.dto.PaymentOrderResponse;
import vn.tera.learn.dto.PlusStatusResponse;
import vn.tera.learn.dto.SepayWebhookPayload;
import vn.tera.learn.entity.PaymentOrder;
import vn.tera.learn.entity.User;
import vn.tera.learn.entity.enums.PaymentOrderStatus;
import vn.tera.learn.repository.PaymentOrderRepository;
import vn.tera.learn.repository.PaymentWebhookEventRepository;
import vn.tera.learn.repository.UserRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional
public class PaymentService {
    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    private static final Pattern PAYMENT_CODE_PATTERN = Pattern.compile("\\bTERA[A-Z0-9]{10}\\b");
    private static final Instant LIFETIME_EXPIRY = Instant.parse("9999-12-31T23:59:59Z");

    private final PaymentOrderRepository paymentOrderRepository;
    private final PaymentWebhookEventRepository webhookEventRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final JdbcTemplate jdbc;

    @Value("${payment.order.expiry-minutes:30}")
    private long orderExpiryMinutes;

    @Value("${payment.sepay.bank:}")
    private String bank;

    @Value("${payment.sepay.account-number:}")
    private String accountNumber;

    @Value("${payment.sepay.account-holder:}")
    private String accountHolder;

    public PaymentService(
            PaymentOrderRepository paymentOrderRepository,
            PaymentWebhookEventRepository webhookEventRepository,
            UserRepository userRepository,
            ObjectMapper objectMapper,
            JdbcTemplate jdbc
    ) {
        this.paymentOrderRepository = paymentOrderRepository;
        this.webhookEventRepository = webhookEventRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
        this.jdbc = jdbc;
    }

    @Transactional(readOnly = true)
    public List<PlusPlanResponse> getPlans() {
        return jdbc.query("""
                SELECT id, code, name, description, amount, duration_days, benefits,
                       special_benefit, status, featured, sort_order
                FROM plus_plan_configs
                WHERE status = 'ACTIVE'
                ORDER BY sort_order, id
                """, (result, row) -> new PlusPlanResponse(
                result.getLong("id"),
                result.getString("code"),
                result.getString("name"),
                result.getString("description"),
                result.getLong("amount"),
                result.getObject("duration_days", Integer.class),
                result.getString("benefits"),
                result.getString("special_benefit"),
                result.getString("status"),
                result.getBoolean("featured"),
                result.getInt("sort_order")
        ));
    }

    public PaymentOrderResponse createOrder(Long userId, String requestedPlanCode) {
        validatePaymentConfiguration();
        PlusPlanConfig plan = findPlanForOrder(normalizePlanCode(requestedPlanCode));
        User user = requireUser(userId);
        Instant now = Instant.now();
        validateUpgrade(userId, user, plan, now);

        Optional<PaymentOrder> existing = paymentOrderRepository
                .findFirstByUserIdAndPlanCodeAndStatusAndExpiresAtAfterOrderByCreatedAtDesc(
                        userId, plan.code(), PaymentOrderStatus.PENDING, now
                );
        if (existing.isPresent()) return toResponse(existing.get());

        PaymentOrder order = new PaymentOrder();
        order.setId(UUID.randomUUID());
        order.setUser(user);
        order.setPlanCode(plan.code());
        order.setPaymentCode(generatePaymentCode());
        order.setAmount(plan.amount());
        order.setStatus(PaymentOrderStatus.PENDING);
        order.setExpiresAt(now.plus(orderExpiryMinutes, ChronoUnit.MINUTES));
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        return toResponse(paymentOrderRepository.save(order));
    }

    public PaymentOrderResponse getOrder(Long userId, UUID orderId) {
        PaymentOrder order = paymentOrderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn thanh toán"));
        if (order.getStatus() == PaymentOrderStatus.PENDING && order.getExpiresAt().isBefore(Instant.now())) {
            order.setStatus(PaymentOrderStatus.EXPIRED);
            order.setUpdatedAt(Instant.now());
        }
        return toResponse(order);
    }

    @Transactional(readOnly = true)
    public PlusStatusResponse getStatus(Long userId) {
        User user = requireUser(userId);
        Instant now = Instant.now();
        Optional<PaymentOrder> currentOrder = paymentOrderRepository
                .findFirstByUserIdAndStatusOrderByPaidAtDescCreatedAtDesc(userId, PaymentOrderStatus.PAID);
        return new PlusStatusResponse(
                isPlusActive(user, now),
                currentOrder.map(PaymentOrder::getPlanCode).orElse(null),
                currentOrder.map(order -> resolvePlanName(order.getPlanCode())).orElse(null),
                user.getPlusStartsAt(),
                user.getPlusExpiresAt()
        );
    }

    public void processSepayWebhook(String rawPayload) {
        SepayWebhookPayload payload = parsePayload(rawPayload);
        validateWebhookPayload(payload);
        if (webhookEventRepository.insertIfAbsent(payload.getId(), rawPayload, Instant.now()) == 0) return;
        if (!"in".equalsIgnoreCase(payload.getTransferType())) {
            log.info("Bỏ qua giao dịch SePay tiền ra {}", payload.getId());
            return;
        }

        String paymentCode = resolvePaymentCode(payload);
        if (paymentCode == null) {
            log.warn("Không tìm thấy mã TERA trong giao dịch SePay {}", payload.getId());
            return;
        }
        Optional<PaymentOrder> optionalOrder = paymentOrderRepository.findByPaymentCodeForUpdate(paymentCode);
        if (optionalOrder.isEmpty()) {
            log.warn("Không tìm thấy đơn {} cho giao dịch SePay {}", paymentCode, payload.getId());
            return;
        }

        PaymentOrder order = optionalOrder.get();
        Instant now = Instant.now();
        if (order.getStatus() != PaymentOrderStatus.PENDING) return;
        if (!now.isBefore(order.getExpiresAt())) {
            order.setStatus(PaymentOrderStatus.EXPIRED);
            order.setUpdatedAt(now);
            log.warn("Bỏ qua giao dịch SePay đến sau khi đơn {} đã hết hạn", paymentCode);
            return;
        }
        if (!matchesConfiguredAccount(accountNumber, payload)) {
            log.warn("Sai tài khoản nhận ở giao dịch SePay {}", payload.getId());
            return;
        }
        if (!order.getAmount().equals(payload.getTransferAmount())) {
            log.warn("Sai số tiền ở giao dịch SePay {}", payload.getId());
            return;
        }

        User user = userRepository.findForPayment(order.getUser().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng"));
        PlusPlanConfig plan = findPlan(order.getPlanCode(), false);
        if (plan == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Gói Tera Plus không còn tồn tại");
        }
        PlusPeriod period = activatePlus(user, plan, now);
        order.setStatus(PaymentOrderStatus.PAID);
        order.setPaidAt(now);
        order.setPlusStartsAt(period.startsAt());
        order.setPlusExpiresAt(period.expiresAt());
        order.setSepayTransactionId(payload.getId());
        order.setBankReferenceCode(payload.getReferenceCode());
        order.setUpdatedAt(now);
    }

    private void validateUpgrade(Long userId, User user, PlusPlanConfig requestedPlan, Instant now) {
        if (!isPlusActive(user, now)) return;
        paymentOrderRepository.findFirstByUserIdAndStatusOrderByPaidAtDescCreatedAtDesc(
                userId, PaymentOrderStatus.PAID
        ).ifPresent(currentOrder -> {
            PlusPlanConfig currentPlan = findPlan(currentOrder.getPlanCode(), false);
            if (currentPlan != null && requestedPlan.sortOrder() <= currentPlan.sortOrder()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Bạn đang dùng gói này. Vui lòng chọn gói cao hơn để nâng cấp."
                );
            }
        });
    }

    private PlusPeriod activatePlus(User user, PlusPlanConfig plan, Instant now) {
        Instant currentExpiry = user.getPlusExpiresAt();
        boolean active = isPlusActive(user, now);
        Instant grantedStartsAt = active && !isLifetime(currentExpiry) ? currentExpiry : now;
        Instant newExpiry;
        if (plan.durationDays() == null || isLifetime(currentExpiry)) {
            newExpiry = LIFETIME_EXPIRY;
        } else {
            Instant start = currentExpiry != null && currentExpiry.isAfter(now) ? currentExpiry : now;
            newExpiry = start.plus(plan.durationDays(), ChronoUnit.DAYS);
        }
        if (!active || user.getPlusStartsAt() == null) user.setPlusStartsAt(now);
        user.setPlusExpiresAt(newExpiry);
        return new PlusPeriod(grantedStartsAt, newExpiry);
    }

    private PaymentOrderResponse toResponse(PaymentOrder order) {
        return new PaymentOrderResponse(
                order.getId(),
                order.getPlanCode(),
                resolvePlanName(order.getPlanCode()),
                order.getAmount(),
                "VND",
                order.getStatus(),
                order.getPaymentCode(),
                buildQrCodeUrl(order),
                bank,
                accountNumber,
                accountHolder,
                order.getExpiresAt(),
                order.getPaidAt(),
                order.getPlusStartsAt() != null ? order.getPlusStartsAt() : order.getUser().getPlusStartsAt(),
                order.getPlusExpiresAt() != null ? order.getPlusExpiresAt() : order.getUser().getPlusExpiresAt()
        );
    }

    private String buildQrCodeUrl(PaymentOrder order) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString("https://qr.sepay.vn/img")
                .queryParam("acc", accountNumber)
                .queryParam("bank", bank)
                .queryParam("amount", order.getAmount())
                .queryParam("des", order.getPaymentCode())
                .queryParam("template", "compact")
                .queryParam("showinfo", true);
        if (accountHolder != null && !accountHolder.isBlank()) builder.queryParam("holder", accountHolder);
        return builder.build().encode().toUriString();
    }

    private SepayWebhookPayload parsePayload(String rawPayload) {
        try {
            return objectMapper.readValue(rawPayload, SepayWebhookPayload.class);
        } catch (JsonProcessingException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dữ liệu webhook SePay không hợp lệ");
        }
    }

    private void validateWebhookPayload(SepayWebhookPayload payload) {
        if (payload.getId() == null || payload.getTransferAmount() == null || payload.getAccountNumber() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Webhook SePay thiếu dữ liệu bắt buộc");
        }
    }

    private String resolvePaymentCode(SepayWebhookPayload payload) {
        if (payload.getCode() != null) {
            String code = payload.getCode().trim().toUpperCase(Locale.ROOT);
            if (PAYMENT_CODE_PATTERN.matcher(code).matches()) return code;
        }
        if (payload.getContent() == null) return null;
        Matcher matcher = PAYMENT_CODE_PATTERN.matcher(payload.getContent().toUpperCase(Locale.ROOT));
        return matcher.find() ? matcher.group() : null;
    }

    private String generatePaymentCode() {
        return "TERA" + UUID.randomUUID().toString().replace("-", "")
                .substring(0, 10).toUpperCase(Locale.ROOT);
    }

    static boolean matchesConfiguredAccount(String configuredAccount, SepayWebhookPayload payload) {
        String expected = normalizeAccount(configuredAccount);
        return expected.equals(normalizeAccount(payload.getAccountNumber()))
                || expected.equals(normalizeAccount(payload.getSubAccount()));
    }

    private static String normalizeAccount(String value) {
        return value == null ? "" : value.replaceAll("\\s+", "").toUpperCase(Locale.ROOT);
    }

    private void validatePaymentConfiguration() {
        if (bank == null || bank.isBlank() || accountNumber == null || accountNumber.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Tài khoản nhận thanh toán SePay chưa được cấu hình"
            );
        }
    }

    private PlusPlanConfig findPlan(String planCode, boolean activeOnly) {
        try {
            String sql = """
                    SELECT code, name, amount, duration_days, sort_order
                    FROM plus_plan_configs
                    WHERE code = ?
                    """ + (activeOnly ? " AND status = 'ACTIVE'" : "");
            return jdbc.queryForObject(sql, (result, row) -> new PlusPlanConfig(
                    result.getString("code"),
                    result.getString("name"),
                    result.getLong("amount"),
                    result.getObject("duration_days", Integer.class),
                    result.getInt("sort_order")
            ), planCode);
        } catch (EmptyResultDataAccessException exception) {
            if (activeOnly) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Gói Tera Plus không hợp lệ hoặc đã tắt");
            }
            return null;
        }
    }

    private PlusPlanConfig findPlanForOrder(String planCode) {
        try {
            return jdbc.queryForObject("""
                    SELECT code, name, amount, duration_days, sort_order
                    FROM plus_plan_configs
                    WHERE code = ? AND status = 'ACTIVE'
                    FOR SHARE
                    """, (result, row) -> new PlusPlanConfig(
                    result.getString("code"),
                    result.getString("name"),
                    result.getLong("amount"),
                    result.getObject("duration_days", Integer.class),
                    result.getInt("sort_order")
            ), planCode);
        } catch (EmptyResultDataAccessException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Gói Tera Plus không hợp lệ hoặc đã tắt");
        }
    }

    private String resolvePlanName(String planCode) {
        PlusPlanConfig plan = findPlan(planCode, false);
        return plan == null ? planCode : plan.name();
    }

    private String normalizePlanCode(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private boolean isPlusActive(User user, Instant now) {
        boolean started = user.getPlusStartsAt() == null || !user.getPlusStartsAt().isAfter(now);
        return started && user.getPlusExpiresAt() != null && user.getPlusExpiresAt().isAfter(now);
    }

    private boolean isLifetime(Instant expiry) {
        return expiry != null && !expiry.isBefore(LIFETIME_EXPIRY);
    }

    private User requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng"));
    }

    public record PlusPlanResponse(
            long id,
            String code,
            String name,
            String description,
            long amount,
            Integer durationDays,
            String benefits,
            String specialBenefit,
            String status,
            boolean featured,
            int sortOrder
    ) {}

    private record PlusPlanConfig(String code, String name, long amount, Integer durationDays, int sortOrder) {}
    private record PlusPeriod(Instant startsAt, Instant expiresAt) {}
}
