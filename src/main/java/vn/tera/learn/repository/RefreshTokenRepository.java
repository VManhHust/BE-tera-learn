package vn.tera.learn.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.tera.learn.entity.RefreshToken;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("DELETE FROM RefreshToken token WHERE token.user.id = :userId AND (token.revoked = true OR token.expiresAt < :now)")
    void deleteExpiredOrRevokedByUserId(@Param("userId") Long userId, @Param("now") Instant now);

    @Query("SELECT COUNT(token) FROM RefreshToken token WHERE token.user.id = :userId AND token.revoked = false AND token.expiresAt >= :now")
    long countActiveByUserId(@Param("userId") Long userId, @Param("now") Instant now);

    @Query("SELECT token FROM RefreshToken token WHERE token.user.id = :userId AND token.revoked = false AND token.expiresAt >= :now ORDER BY token.createdAt ASC")
    List<RefreshToken> findActiveByUserIdOrderByCreatedAtAsc(@Param("userId") Long userId, @Param("now") Instant now);
}
