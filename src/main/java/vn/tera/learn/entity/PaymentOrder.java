package vn.tera.learn.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.tera.learn.entity.enums.PaymentOrderStatus;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "payment_orders")
public class PaymentOrder {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 20)
    private String planCode;

    @Column(nullable = false, unique = true, length = 20)
    private String paymentCode;

    @Column(nullable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentOrderStatus status;

    @Column(nullable = false)
    private Instant expiresAt;

    private Instant paidAt;
    private Instant plusStartsAt;
    private Instant plusExpiresAt;

    @Column(unique = true)
    private Long sepayTransactionId;

    @Column(length = 255)
    private String bankReferenceCode;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

}
