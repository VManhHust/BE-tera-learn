package vn.tera.learn.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import vn.tera.learn.dto.CreatePlusOrderRequest;
import vn.tera.learn.dto.PaymentOrderResponse;
import vn.tera.learn.dto.PlusStatusResponse;
import vn.tera.learn.security.JwtClaims;
import vn.tera.learn.service.PaymentService;
import vn.tera.learn.service.SepayWebhookVerifier;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService paymentService;
    private final SepayWebhookVerifier webhookVerifier;
    private final String webhookSecret;

    public PaymentController(
            PaymentService paymentService,
            SepayWebhookVerifier webhookVerifier,
            @Value("${payment.sepay.webhook-secret:}") String webhookSecret
    ) {
        this.paymentService = paymentService;
        this.webhookVerifier = webhookVerifier;
        this.webhookSecret = webhookSecret;
    }

    @GetMapping("/plus/plans")
    public ResponseEntity<List<PaymentService.PlusPlanResponse>> getPlans() {
        return ResponseEntity.ok(paymentService.getPlans());
    }

    @PostMapping("/plus/orders")
    public ResponseEntity<PaymentOrderResponse> createOrder(
            @AuthenticationPrincipal JwtClaims claims,
            @Valid @RequestBody CreatePlusOrderRequest request
    ) {
        return ResponseEntity.ok(paymentService.createOrder(claims.getUserId(), request.planCode()));
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<PaymentOrderResponse> getOrder(
            @AuthenticationPrincipal JwtClaims claims,
            @PathVariable UUID orderId
    ) {
        return ResponseEntity.ok(paymentService.getOrder(claims.getUserId(), orderId));
    }

    @GetMapping("/plus/status")
    public ResponseEntity<PlusStatusResponse> getStatus(@AuthenticationPrincipal JwtClaims claims) {
        return ResponseEntity.ok(paymentService.getStatus(claims.getUserId()));
    }

    @PostMapping("/sepay/webhook")
    public ResponseEntity<Map<String, Boolean>> receiveSepayWebhook(
            @RequestBody String rawPayload,
            @RequestHeader(name = "X-SePay-Signature", required = false) String signature,
            @RequestHeader(name = "X-SePay-Timestamp", required = false) String timestamp
    ) {
        if (!webhookVerifier.isValid(rawPayload, signature, timestamp, webhookSecret)) {
            throw new ResponseStatusException(UNAUTHORIZED, "Chữ ký SePay không hợp lệ");
        }
        paymentService.processSepayWebhook(rawPayload);
        return ResponseEntity.ok(Map.of("success", true));
    }
}

