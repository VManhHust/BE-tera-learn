package vn.tera.learn.service.impl;

import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.tera.learn.dto.TokenPair;
import vn.tera.learn.entity.RefreshToken;
import vn.tera.learn.entity.User;
import vn.tera.learn.entity.enums.UserStatus;
import vn.tera.learn.repository.RefreshTokenRepository;
import vn.tera.learn.security.JwtClaims;
import vn.tera.learn.security.JwtProvider;
import vn.tera.learn.service.AuthService;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;

@Service
public class AuthServiceImpl implements AuthService {

    private static final int MAX_ACTIVE_SESSIONS = 5;

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProvider jwtProvider;
    private final long refreshTokenExpiration;

    public AuthServiceImpl(
            RefreshTokenRepository refreshTokenRepository,
            JwtProvider jwtProvider,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration
    ) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtProvider = jwtProvider;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    @Override
    @Transactional
    public TokenPair generateTokenPair(User user) {
        ensureCanIssueToken(user);
        String accessToken = jwtProvider.generateAccessToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                user.getDisplayName(),
                user.getStatus().name()
        );
        String refreshToken = jwtProvider.generateRefreshToken(user.getId());
        storeRefreshToken(user, refreshToken);
        return new TokenPair(accessToken, refreshToken);
    }

    @Override
    @Transactional
    public TokenPair refresh(String rawRefreshToken) {
        JwtClaims claims = jwtProvider.validateToken(rawRefreshToken);
        if (!"refresh".equals(claims.getTokenType())) {
            throw new JwtException("Refresh token required");
        }

        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(hashToken(rawRefreshToken))
                .orElseThrow(() -> new JwtException("Token invalid or expired"));

        if (storedToken.isRevoked() || storedToken.getExpiresAt().isBefore(Instant.now())) {
            throw new JwtException("Token invalid or expired");
        }
        if (!storedToken.getUser().getId().equals(claims.getUserId())) {
            throw new JwtException("Token owner invalid");
        }

        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);
        return generateTokenPair(storedToken.getUser());
    }

    @Override
    @Transactional
    public void logout(String rawRefreshToken) {
        refreshTokenRepository.findByTokenHash(hashToken(rawRefreshToken)).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }

    private void storeRefreshToken(User user, String rawRefreshToken) {
        Instant now = Instant.now();
        refreshTokenRepository.deleteExpiredOrRevokedByUserId(user.getId(), now);

        long activeCount = refreshTokenRepository.countActiveByUserId(user.getId(), now);
        if (activeCount >= MAX_ACTIVE_SESSIONS) {
            List<RefreshToken> oldestTokens = refreshTokenRepository
                    .findActiveByUserIdOrderByCreatedAtAsc(user.getId(), now);
            long numberToRevoke = activeCount - MAX_ACTIVE_SESSIONS + 1;
            oldestTokens.stream().limit(numberToRevoke).forEach(token -> {
                token.setRevoked(true);
                refreshTokenRepository.save(token);
            });
        }

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(hashToken(rawRefreshToken));
        refreshToken.setExpiresAt(now.plusSeconds(refreshTokenExpiration));
        refreshTokenRepository.save(refreshToken);
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(rawToken.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 not available", exception);
        }
    }

    private void ensureCanIssueToken(User user) {
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BadCredentialsException("Tài khoản không hoạt động");
        }
    }
}
