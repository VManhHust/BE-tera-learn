package vn.tera.learn.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import vn.tera.learn.dto.TokenPair;
import vn.tera.learn.entity.User;
import vn.tera.learn.entity.enums.UserStatus;
import vn.tera.learn.repository.RefreshTokenRepository;
import vn.tera.learn.repository.UserRepository;
import vn.tera.learn.service.AuthService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {"debug=false", "logging.level.root=INFO"})
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private AuthService authService;

    @BeforeEach
    void cleanDatabase() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void rejectsProtectedResourceWithoutAccessToken() throws Exception {
        mockMvc.perform(get("/api/protected-resource"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void handlesCorsPreflightInsideSecurityConfig() throws Exception {
        mockMvc.perform(options("/api/protected-resource")
                        .header(HttpHeaders.ORIGIN, "http://localhost:3000")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:3000"));
    }

    @Test
    void allowsValidAccessTokenThroughSecurityFilter() throws Exception {
        TokenPair tokenPair = createTokenPair();

        mockMvc.perform(get("/api/protected-resource")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenPair.getAccessToken()))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectsRefreshTokenUsedAsBearerToken() throws Exception {
        TokenPair tokenPair = createTokenPair();

        mockMvc.perform(get("/api/protected-resource")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenPair.getRefreshToken()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Token invalid or expired"));
    }

    @Test
    void rejectsTokenAfterAccountIsLocked() throws Exception {
        User user = activeUser();
        user = userRepository.save(user);
        TokenPair tokenPair = authService.generateTokenPair(user);
        user.setStatus(UserStatus.LOCK);
        userRepository.saveAndFlush(user);

        mockMvc.perform(get("/api/protected-resource")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenPair.getAccessToken()))
                .andExpect(status().isUnauthorized());
    }

    private TokenPair createTokenPair() {
        return authService.generateTokenPair(userRepository.save(activeUser()));
    }

    private User activeUser() {
        User user = new User();
        user.setEmail("student@tera.vn");
        user.setDisplayName("Học viên Tera");
        return user;
    }
}
