package vn.tera.learn.security;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import vn.tera.learn.dto.TokenPair;
import vn.tera.learn.entity.User;
import vn.tera.learn.repository.RefreshTokenRepository;
import vn.tera.learn.repository.UserRepository;
import vn.tera.learn.service.AuthService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {"debug=false", "logging.level.root=INFO"})
@AutoConfigureMockMvc
class AuthRefreshIntegrationTest {

    private static final String REFRESH_COOKIE = "tera_user_refresh_token";

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
    void rotatesRefreshTokenAndRejectsReuse() throws Exception {
        User newUser = new User();
        newUser.setEmail("refresh@tera.vn");
        newUser.setDisplayName("Refresh User");
        User user = userRepository.save(newUser);
        TokenPair original = authService.generateTokenPair(user);
        Cookie originalCookie = new Cookie(REFRESH_COOKIE, original.getRefreshToken());

        mockMvc.perform(post("/api/auth/refresh").cookie(originalCookie))
                .andExpect(status().isOk())
                .andExpect(cookie().exists(REFRESH_COOKIE))
                .andExpect(cookie().httpOnly(REFRESH_COOKIE, true))
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty());

        mockMvc.perform(post("/api/auth/refresh").cookie(originalCookie))
                .andExpect(status().isUnauthorized())
                .andExpect(cookie().maxAge(REFRESH_COOKIE, 0));
    }
}
