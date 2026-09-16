package vn.tera.learn.controller;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.tera.learn.dto.ErrorResponse;
import vn.tera.learn.dto.EmailAddressRequest;
import vn.tera.learn.dto.EmailLoginRequest;
import vn.tera.learn.dto.EmailRegisterRequest;
import vn.tera.learn.dto.EmailStepResponse;
import vn.tera.learn.dto.TokenPair;
import vn.tera.learn.dto.VerificationChallengeResponse;
import vn.tera.learn.dto.VerifyEmailRequest;
import vn.tera.learn.entity.User;
import vn.tera.learn.entity.enums.UserStatus;
import vn.tera.learn.repository.UserRepository;
import vn.tera.learn.service.AuthService;
import vn.tera.learn.service.EmailAuthService;
import vn.tera.learn.service.GoogleOAuthService;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String REFRESH_COOKIE_NAME = "tera_user_refresh_token";
    private static final long OAUTH_SESSION_CODE_TTL_SECONDS = 120;

    private final AuthService authService;
    private final EmailAuthService emailAuthService;
    private final GoogleOAuthService googleOAuthService;
    private final UserRepository userRepository;
    private final String frontendUrl;
    private final boolean secureCookies;
    private final SecureRandom secureRandom = new SecureRandom();
    private final Map<String, OAuthSession> pendingOAuthSessions = new ConcurrentHashMap<>();

    public AuthController(
            AuthService authService,
            EmailAuthService emailAuthService,
            GoogleOAuthService googleOAuthService,
            UserRepository userRepository,
            @Value("${frontend.url:http://localhost:3000}") String frontendUrl,
            @Value("${app.cookie.secure:false}") boolean secureCookies
    ) {
        this.authService = authService;
        this.emailAuthService = emailAuthService;
        this.googleOAuthService = googleOAuthService;
        this.userRepository = userRepository;
        this.frontendUrl = frontendUrl;
        this.secureCookies = secureCookies;
    }

    @PostMapping("/email/resolve")
    public ResponseEntity<EmailStepResponse> resolveEmail(@Valid @RequestBody EmailAddressRequest request) {
        return ResponseEntity.ok(emailAuthService.resolve(request.getEmail()));
    }

    @PostMapping("/email/register")
    public ResponseEntity<VerificationChallengeResponse> registerWithEmail(
            @Valid @RequestBody EmailRegisterRequest request
    ) {
        return ResponseEntity.ok(emailAuthService.startRegistration(request));
    }

    @PostMapping("/email/verification/resend")
    public ResponseEntity<VerificationChallengeResponse> resendEmailVerification(
            @Valid @RequestBody EmailAddressRequest request
    ) {
        return ResponseEntity.ok(emailAuthService.resendVerification(request.getEmail()));
    }

    @PostMapping("/email/verification/confirm")
    public ResponseEntity<TokenPair> confirmEmailVerification(
            @Valid @RequestBody VerifyEmailRequest request,
            HttpServletResponse response
    ) {
        TokenPair tokenPair = emailAuthService.verifyEmail(request.getEmail(), request.getCode());
        setRefreshCookie(response, tokenPair.getRefreshToken());
        return ResponseEntity.ok(tokenPair);
    }

    @PostMapping("/email/login")
    public ResponseEntity<TokenPair> loginWithEmail(
            @Valid @RequestBody EmailLoginRequest request,
            HttpServletResponse response
    ) {
        TokenPair tokenPair = emailAuthService.login(request);
        setRefreshCookie(response, tokenPair.getRefreshToken());
        return ResponseEntity.ok(tokenPair);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(
            @CookieValue(name = REFRESH_COOKIE_NAME, required = false) String refreshToken,
            HttpServletResponse response
    ) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.status(401).body(new ErrorResponse("Token invalid or expired"));
        }
        try {
            TokenPair tokenPair = authService.refresh(refreshToken);
            setRefreshCookie(response, tokenPair.getRefreshToken());
            return ResponseEntity.ok(tokenPair);
        } catch (JwtException | AuthenticationException exception) {
            clearRefreshCookie(response);
            return ResponseEntity.status(401).body(new ErrorResponse("Token invalid or expired"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = REFRESH_COOKIE_NAME, required = false) String refreshToken,
            HttpServletResponse response
    ) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            authService.logout(refreshToken);
        }
        clearRefreshCookie(response);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/google")
    public void redirectToGoogle(HttpServletResponse response) throws IOException {
        try {
            response.sendRedirect(googleOAuthService.buildAuthorizationUrl());
        } catch (IllegalStateException exception) {
            response.sendRedirect(frontendUrl + "/login?error=oauth_not_configured");
        }
    }

    @GetMapping("/callback/google")
    public void handleGoogleCallback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String state,
            HttpServletResponse response
    ) throws IOException {
        if (!googleOAuthService.isValidState(state) || error != null || code == null) {
            response.sendRedirect(frontendUrl + "/login?error=oauth_failed");
            return;
        }

        try {
            GoogleOAuthService.GoogleUserInfo userInfo = googleOAuthService.exchangeCodeForUserInfo(code);
            User user = userRepository.findByEmailIgnoreCase(userInfo.getEmail())
                    .map(existingUser -> updateGoogleProfile(existingUser, userInfo))
                    .orElseGet(() -> userRepository.save(createGoogleUser(userInfo)));

            TokenPair tokenPair = authService.generateTokenPair(user);
            setRefreshCookie(response, tokenPair.getRefreshToken());
            response.sendRedirect(frontendUrl + "/auth/callback?code=" + encode(createOAuthSessionCode(tokenPair)));
        } catch (Exception exception) {
            response.sendRedirect(frontendUrl + "/login?error=oauth_failed");
        }
    }

    @PostMapping("/oauth/session")
    public ResponseEntity<?> exchangeOAuthSession(@RequestBody Map<String, String> request) {
        String code = request.get("code");
        if (code == null || code.isBlank()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("OAuth session code is required"));
        }

        OAuthSession session = pendingOAuthSessions.remove(code);
        if (session == null || session.getExpiresAt().isBefore(Instant.now())) {
            return ResponseEntity.status(401).body(new ErrorResponse("OAuth session code invalid or expired"));
        }
        return ResponseEntity.ok(session.getTokenPair());
    }

    private User updateGoogleProfile(User user, GoogleOAuthService.GoogleUserInfo userInfo) {
        user.setGoogleId(userInfo.getGoogleId());
        if (user.getStatus() == UserStatus.PENDING) {
            user.setStatus(UserStatus.ACTIVE);
        }
        if (user.getDisplayName() == null || user.getDisplayName().isBlank()) {
            user.setDisplayName(userInfo.getName());
        }
        return userRepository.save(user);
    }

    private User createGoogleUser(GoogleOAuthService.GoogleUserInfo userInfo) {
        User user = new User();
        user.setEmail(userInfo.getEmail());
        user.setDisplayName(userInfo.getName());
        user.setGoogleId(userInfo.getGoogleId());
        return user;
    }

    private String createOAuthSessionCode(TokenPair tokenPair) {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        String code = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        Instant now = Instant.now();
        pendingOAuthSessions.entrySet().removeIf(entry -> entry.getValue().getExpiresAt().isBefore(now));
        pendingOAuthSessions.put(code, new OAuthSession(tokenPair, now.plusSeconds(OAUTH_SESSION_CODE_TTL_SECONDS)));
        return code;
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private void setRefreshCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie(REFRESH_COOKIE_NAME, token);
        cookie.setMaxAge(60 * 60 * 24 * 7);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(secureCookies);
        cookie.setAttribute("SameSite", "Lax");
        response.addCookie(cookie);
    }

    private void clearRefreshCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(REFRESH_COOKIE_NAME, "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(secureCookies);
        cookie.setAttribute("SameSite", "Lax");
        response.addCookie(cookie);
    }

    private static class OAuthSession {

        private final TokenPair tokenPair;
        private final Instant expiresAt;

        private OAuthSession(TokenPair tokenPair, Instant expiresAt) {
            this.tokenPair = tokenPair;
            this.expiresAt = expiresAt;
        }

        private TokenPair getTokenPair() {
            return tokenPair;
        }

        private Instant getExpiresAt() {
            return expiresAt;
        }
    }
}
