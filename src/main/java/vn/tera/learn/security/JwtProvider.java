package vn.tera.learn.security;

public interface JwtProvider {
    String generateAccessToken(Long userId, String email, String role, String displayName, String status);

    String generateRefreshToken(Long userId);

    JwtClaims validateToken(String token);
}
