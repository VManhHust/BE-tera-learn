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
import vn.tera.learn.entity.enums.PaymentOrderStatus;

import java.time.Instant;
import java.util.UUID;

@Entity
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

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getPlanCode() { return planCode; }
    public void setPlanCode(String planCode) { this.planCode = planCode; }
    public String getPaymentCode() { return paymentCode; }
    public void setPaymentCode(String paymentCode) { this.paymentCode = paymentCode; }
    public Long getAmount() { return amount; }
    public void setAmount(Long amount) { this.amount = amount; }
    public PaymentOrderStatus getStatus() { return status; }
    public void setStatus(PaymentOrderStatus status) { this.status = status; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public Instant getPaidAt() { return paidAt; }
    public void setPaidAt(Instant paidAt) { this.paidAt = paidAt; }
    public Instant getPlusStartsAt() { return plusStartsAt; }
    public void setPlusStartsAt(Instant plusStartsAt) { this.plusStartsAt = plusStartsAt; }
    public Instant getPlusExpiresAt() { return plusExpiresAt; }
    public void setPlusExpiresAt(Instant plusExpiresAt) { this.plusExpiresAt = plusExpiresAt; }
    public Long getSepayTransactionId() { return sepayTransactionId; }
    public void setSepayTransactionId(Long sepayTransactionId) { this.sepayTransactionId = sepayTransactionId; }
    public String getBankReferenceCode() { return bankReferenceCode; }
    public void setBankReferenceCode(String bankReferenceCode) { this.bankReferenceCode = bankReferenceCode; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}

