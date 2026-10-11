package vn.tera.learn.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import vn.tera.learn.entity.enums.PaymentOrderStatus;

import java.time.Instant;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentOrderResponse {

    private UUID orderId;
    private String planCode;
    private String planName;
    private long amount;
    private String currency;
    private PaymentOrderStatus status;
    private String paymentCode;
    private String qrCodeUrl;
    private String bank;
    private String accountNumber;
    private String accountHolder;
    private Instant expiresAt;
    private Instant paidAt;
    private Instant plusStartsAt;
    private Instant plusExpiresAt;
}
