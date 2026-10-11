package vn.tera.learn.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
@Table(name = "payment_webhook_events")
public class PaymentWebhookEvent {
    @Id
    private Long transactionId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String rawPayload;

    @Column(nullable = false)
    private Instant receivedAt;

}
