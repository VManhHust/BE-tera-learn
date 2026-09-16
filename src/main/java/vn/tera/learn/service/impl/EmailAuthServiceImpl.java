package vn.tera.learn.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.tera.learn.dto.EmailLoginRequest;
import vn.tera.learn.dto.EmailRegisterRequest;
import vn.tera.learn.dto.EmailStepResponse;
import vn.tera.learn.dto.TokenPair;
import vn.tera.learn.dto.VerificationChallengeResponse;
import vn.tera.learn.entity.EmailVerification;
import vn.tera.learn.entity.User;
import vn.tera.learn.entity.enums.UserStatus;
import vn.tera.learn.exception.AuthFlowException;
import vn.tera.learn.repository.EmailVerificationRepository;
import vn.tera.learn.repository.UserRepository;
import vn.tera.learn.service.AuthService;
import vn.tera.learn.service.EmailAuthService;
import vn.tera.learn.service.VerificationEmailSender;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;

@Service
public class EmailAuthServiceImpl implements EmailAuthService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final UserRepository userRepository;
    private final EmailVerificationRepository verificationRepository;
    private final VerificationEmailSender emailSender;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;
    private final SecureRandom secureRandom = new SecureRandom();
    private final byte[] codeSecret;
    private final long ttlSeconds;
    private final long resendCooldownSeconds;
    private final int maxAttempts;

    public EmailAuthServiceImpl(
            UserRepository userRepository,
            EmailVerificationRepository verificationRepository,
            VerificationEmailSender emailSender,
            PasswordEncoder passwordEncoder,
            AuthService authService,
            @Value("${email.verification.code-secret}") String codeSecret,
            @Value("${email.verification.ttl-seconds:600}") long ttlSeconds,
            @Value("${email.verification.resend-cooldown-seconds:60}") long resendCooldownSeconds,
            @Value("${email.verification.max-attempts:5}") int maxAttempts
    ) {
        this.userRepository = userRepository;
        this.verificationRepository = verificationRepository;
        this.emailSender = emailSender;
        this.passwordEncoder = passwordEncoder;
        this.authService = authService;
        this.codeSecret = codeSecret.getBytes(StandardCharsets.UTF_8);
        this.ttlSeconds = ttlSeconds;
        this.resendCooldownSeconds = resendCooldownSeconds;
        this.maxAttempts = maxAttempts;
    }

    @Override
    @Transactional(readOnly = true)
    public EmailStepResponse resolve(String rawEmail) {
        String email = normalizeEmail(rawEmail);
        return userRepository.findByEmailIgnoreCase(email)
                .filter(user -> user.getStatus() != UserStatus.PENDING)
                .map(user -> new EmailStepResponse("LOGIN"))
                .orElseGet(() -> new EmailStepResponse("REGISTER"));
    }

    @Override
    @Transactional
    public VerificationChallengeResponse startRegistration(EmailRegisterRequest request) {
        String email = normalizeEmail(request.getEmail());
        ensurePasswordCanBeEncoded(request.getPassword());

        User user = userRepository.findByEmailIgnoreCase(email).orElseGet(User::new);
        if (user.getId() != null && user.getStatus() != UserStatus.PENDING) {
            throw new AuthFlowException(HttpStatus.CONFLICT, "EMAIL_ALREADY_REGISTERED", "Email này đã có tài khoản Tera");
        }

        user.setEmail(email);
        user.setDisplayName(normalizeDisplayName(request.getDisplayName()));
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setStatus(UserStatus.PENDING);
        userRepository.save(user);

        return issueVerification(email);
    }

    @Override
    @Transactional
    public VerificationChallengeResponse resendVerification(String rawEmail) {
        String email = normalizeEmail(rawEmail);
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new AuthFlowException(HttpStatus.NOT_FOUND, "REGISTRATION_NOT_FOUND", "Không tìm thấy đăng ký đang chờ xác thực"));
        if (user.getStatus() != UserStatus.PENDING) {
            throw new AuthFlowException(HttpStatus.CONFLICT, "EMAIL_ALREADY_VERIFIED", "Email này đã được xác thực");
        }
        return issueVerification(email);
    }

    @Override
    @Transactional(noRollbackFor = AuthFlowException.class)
    public TokenPair verifyEmail(String rawEmail, String code) {
        String email = normalizeEmail(rawEmail);
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> invalidCode("Mã xác thực không hợp lệ hoặc đã hết hạn"));
        if (user.getStatus() != UserStatus.PENDING) {
            throw new AuthFlowException(HttpStatus.CONFLICT, "EMAIL_ALREADY_VERIFIED", "Email này đã được xác thực");
        }

        EmailVerification verification = verificationRepository
                .findFirstByEmailAndUsedFalseOrderByCreatedAtDesc(email)
                .orElseThrow(() -> invalidCode("Mã xác thực không hợp lệ hoặc đã hết hạn"));

        Instant now = Instant.now();
        if (verification.getExpiresAt().isBefore(now) || verification.getAttempts() >= maxAttempts) {
            verification.setUsed(true);
            verificationRepository.save(verification);
            throw invalidCode("Mã xác thực không hợp lệ hoặc đã hết hạn");
        }

        if (!matchesCode(email, code, verification.getCodeHash())) {
            verification.setAttempts(verification.getAttempts() + 1);
            if (verification.getAttempts() >= maxAttempts) verification.setUsed(true);
            verificationRepository.save(verification);
            int remaining = Math.max(maxAttempts - verification.getAttempts(), 0);
            throw new AuthFlowException(HttpStatus.BAD_REQUEST, "VERIFICATION_CODE_INVALID",
                    remaining == 0 ? "Mã xác thực đã bị khóa, vui lòng gửi lại mã" : "Mã xác thực không đúng. Bạn còn " + remaining + " lần thử");
        }

        verification.setUsed(true);
        verificationRepository.save(verification);
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
        return authService.generateTokenPair(user);
    }

    @Override
    @Transactional
    public TokenPair login(EmailLoginRequest request) {
        String email = normalizeEmail(request.getEmail());
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(this::invalidCredentials);

        if (user.getStatus() == UserStatus.PENDING) {
            throw new AuthFlowException(HttpStatus.FORBIDDEN, "EMAIL_NOT_VERIFIED", "Email chưa được xác thực");
        }
        if (user.getStatus() != UserStatus.ACTIVE
                || user.getPasswordHash() == null
                || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw invalidCredentials();
        }
        return authService.generateTokenPair(user);
    }

    private VerificationChallengeResponse issueVerification(String email) {
        Instant now = Instant.now();
        verificationRepository.findFirstByEmailOrderByCreatedAtDesc(email).ifPresent(latest -> {
            Instant allowedAt = latest.getCreatedAt().plusSeconds(resendCooldownSeconds);
            if (allowedAt.isAfter(now)) {
                long retryAfter = Math.max(Duration.between(now, allowedAt).toSeconds() + 1, 1);
                throw new AuthFlowException(HttpStatus.TOO_MANY_REQUESTS, "VERIFICATION_RATE_LIMITED",
                        "Vui lòng chờ trước khi gửi lại mã", retryAfter);
            }
        });

        List<EmailVerification> outstanding = verificationRepository.findByEmailAndUsedFalse(email);
        outstanding.forEach(item -> item.setUsed(true));
        verificationRepository.saveAll(outstanding);

        String code = String.format(Locale.ROOT, "%04d", secureRandom.nextInt(10_000));
        EmailVerification verification = new EmailVerification();
        verification.setEmail(email);
        verification.setCodeHash(hashCode(email, code));
        verification.setExpiresAt(now.plusSeconds(ttlSeconds));
        verification.setCreatedAt(now);
        verificationRepository.save(verification);

        emailSender.sendVerificationCode(email, code, Math.max(ttlSeconds / 60, 1));
        return new VerificationChallengeResponse(ttlSeconds, resendCooldownSeconds);
    }

    private boolean matchesCode(String email, String code, String expectedHash) {
        byte[] actual = hashCode(email, code).getBytes(StandardCharsets.US_ASCII);
        byte[] expected = expectedHash.getBytes(StandardCharsets.US_ASCII);
        return MessageDigest.isEqual(actual, expected);
    }

    private String hashCode(String email, String code) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(codeSecret, HMAC_ALGORITHM));
            return HexFormat.of().formatHex(mac.doFinal((email + ":" + code).getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot hash verification code", exception);
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeDisplayName(String displayName) {
        return displayName.trim().replaceAll("\\s+", " ");
    }

    private void ensurePasswordCanBeEncoded(String password) {
        int byteLength = password.getBytes(StandardCharsets.UTF_8).length;
        if (byteLength > 72) {
            throw new AuthFlowException(HttpStatus.BAD_REQUEST, "PASSWORD_TOO_LONG", "Mật khẩu quá dài");
        }
    }

    private AuthFlowException invalidCredentials() {
        return new AuthFlowException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Email hoặc mật khẩu không đúng");
    }

    private AuthFlowException invalidCode(String message) {
        return new AuthFlowException(HttpStatus.BAD_REQUEST, "VERIFICATION_CODE_INVALID", message);
    }
}
