package vn.tera.learn.service;

import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HexFormat;

import static org.assertj.core.api.Assertions.assertThat;

class SepayWebhookVerifierTest {
    private static final Instant NOW = Instant.parse("2026-09-11T10:00:00Z");
    private static final String SECRET = "tera-sepay-test-secret";
    private final SepayWebhookVerifier verifier = new SepayWebhookVerifier(
            Clock.fixed(NOW, ZoneOffset.UTC)
    );

    @Test
    void acceptsCorrectSignatureInsideAllowedTimeWindow() throws Exception {
        String body = "{\"id\":123,\"transferType\":\"in\",\"transferAmount\":69000}";
        String timestamp = Long.toString(NOW.getEpochSecond());

        assertThat(verifier.isValid(body, sign(timestamp, body), timestamp, SECRET)).isTrue();
    }

    @Test
    void rejectsTamperedPayload() throws Exception {
        String originalBody = "{\"id\":123,\"transferAmount\":69000}";
        String timestamp = Long.toString(NOW.getEpochSecond());

        assertThat(verifier.isValid(
                "{\"id\":123,\"transferAmount\":1}",
                sign(timestamp, originalBody),
                timestamp,
                SECRET
        )).isFalse();
    }

    @Test
    void rejectsSignatureOutsideFiveMinuteWindow() throws Exception {
        String body = "{\"id\":123}";
        String staleTimestamp = Long.toString(NOW.minusSeconds(301).getEpochSecond());

        assertThat(verifier.isValid(body, sign(staleTimestamp, body), staleTimestamp, SECRET)).isFalse();
    }

    private String sign(String timestamp, String body) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] hash = mac.doFinal((timestamp + "." + body).getBytes(StandardCharsets.UTF_8));
        return "sha256=" + HexFormat.of().formatHex(hash);
    }
}
