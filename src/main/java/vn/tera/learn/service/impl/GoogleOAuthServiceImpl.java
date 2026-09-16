package vn.tera.learn.service.impl;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import vn.tera.learn.service.GoogleOAuthService;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GoogleOAuthServiceImpl implements GoogleOAuthService {

    private static final String GOOGLE_AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String GOOGLE_USER_INFO_URL = "https://www.googleapis.com/oauth2/v2/userinfo";
    private static final long STATE_TTL_SECONDS = 600;

    private final Map<String, Instant> pendingStates = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();
    private final RestClient restClient = RestClient.create();
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;

    public GoogleOAuthServiceImpl(
            @Value("${google.oauth.client-id:}") String clientId,
            @Value("${google.oauth.client-secret:}") String clientSecret,
            @Value("${google.oauth.redirect-uri}") String redirectUri
    ) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
    }

    @Override
    public String buildAuthorizationUrl() {
        ensureConfigured();
        byte[] bytes = new byte[24];
        secureRandom.nextBytes(bytes);
        String state = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        Instant now = Instant.now();
        pendingStates.entrySet().removeIf(entry -> entry.getValue().isBefore(now));
        pendingStates.put(state, now.plusSeconds(STATE_TTL_SECONDS));

        return UriComponentsBuilder.fromUriString(GOOGLE_AUTH_URL)
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("response_type", "code")
                .queryParam("scope", "email profile")
                .queryParam("state", state)
                .build()
                .encode()
                .toUriString();
    }

    @Override
    public boolean isValidState(String state) {
        if (state == null) {
            return false;
        }
        Instant expiry = pendingStates.remove(state);
        return expiry != null && expiry.isAfter(Instant.now());
    }

    @Override
    public GoogleUserInfo exchangeCodeForUserInfo(String authorizationCode) {
        ensureConfigured();
        try {
            MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
            requestBody.add("code", authorizationCode);
            requestBody.add("client_id", clientId);
            requestBody.add("client_secret", clientSecret);
            requestBody.add("redirect_uri", redirectUri);
            requestBody.add("grant_type", "authorization_code");

            TokenResponse tokenResponse = restClient.post()
                    .uri(GOOGLE_TOKEN_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(TokenResponse.class);

            if (tokenResponse == null || tokenResponse.getAccessToken() == null
                    || tokenResponse.getAccessToken().isBlank()) {
                throw new IllegalStateException("Google did not return an access token");
            }

            UserInfoResponse userInfo = restClient.get()
                    .uri(GOOGLE_USER_INFO_URL)
                    .headers(headers -> headers.setBearerAuth(tokenResponse.getAccessToken()))
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(UserInfoResponse.class);

            if (userInfo == null || userInfo.getEmail() == null || userInfo.getEmail().isBlank()) {
                throw new IllegalStateException("Google did not return an email address");
            }

            return new GoogleUserInfo(userInfo.getEmail(), userInfo.getName(), userInfo.getId());
        } catch (Exception exception) {
            throw new IllegalStateException("oauth_failed", exception);
        }
    }

    private void ensureConfigured() {
        if (clientId.isBlank() || clientSecret.isBlank()) {
            throw new IllegalStateException("Google OAuth is not configured");
        }
    }

    private static class TokenResponse {

        @JsonProperty("access_token")
        private String accessToken;

        public TokenResponse() {
        }

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }
    }

    private static class UserInfoResponse {

        private String id;
        private String email;
        private String name;

        public UserInfoResponse() {
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
