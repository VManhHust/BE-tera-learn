package vn.tera.learn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tera.learn.entity.EmailVerification;

import java.util.List;
import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {
    Optional<EmailVerification> findFirstByEmailOrderByCreatedAtDesc(String email);
    Optional<EmailVerification> findFirstByEmailAndUsedFalseOrderByCreatedAtDesc(String email);
    List<EmailVerification> findByEmailAndUsedFalse(String email);
}
