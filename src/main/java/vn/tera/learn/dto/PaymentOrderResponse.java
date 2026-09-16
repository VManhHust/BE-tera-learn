package vn.tera.learn.dto;

import vn.tera.learn.entity.enums.PaymentOrderStatus;

import java.time.Instant;
import java.util.UUID;

public record PaymentOrderResponse(
        UUID orderId,
        String planCode,
        String planName,
        long amount,
        String currency,
        PaymentOrderStatus status,
        String paymentCode,
        String qrCodeUrl,
        String bank,
        String accountNumber,
        String accountHolder,
        Instant expiresAt,
        Instant paidAt,
        Instant plusStartsAt,
        Instant plusExpiresAt
) {}

