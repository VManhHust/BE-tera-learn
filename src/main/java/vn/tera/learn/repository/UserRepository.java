package vn.tera.learn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.tera.learn.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findForSessionStart(@org.springframework.data.repository.query.Param("id") Long id);
    Optional<User> findByEmail(String email);

    Optional<User> findByEmailIgnoreCase(String email);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findForPayment(@org.springframework.data.repository.query.Param("id") Long id);
}
