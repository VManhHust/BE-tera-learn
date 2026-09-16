package vn.tera.learn.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.tera.learn.entity.PaymentOrder;
import vn.tera.learn.entity.enums.PaymentOrderStatus;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, UUID> {
    Optional<PaymentOrder> findByIdAndUserId(UUID id, Long userId);

    Optional<PaymentOrder> findFirstByUserIdAndPlanCodeAndStatusAndExpiresAtAfterOrderByCreatedAtDesc(
            Long userId,
            String planCode,
            PaymentOrderStatus status,
            Instant now
    );

    Optional<PaymentOrder> findFirstByUserIdAndStatusOrderByPaidAtDescCreatedAtDesc(
            Long userId,
            PaymentOrderStatus status
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT payment FROM PaymentOrder payment WHERE payment.paymentCode = :paymentCode")
    Optional<PaymentOrder> findByPaymentCodeForUpdate(@Param("paymentCode") String paymentCode);
}

