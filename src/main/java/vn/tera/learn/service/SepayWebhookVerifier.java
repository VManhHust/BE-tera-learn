package vn.tera.learn.service;

import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.util.HexFormat;

@Component
public class SepayWebhookVerifier {
    private static final long MAX_TIMESTAMP_DRIFT_SECONDS = 300;
    private static final String HMAC_SHA_256 = "HmacSHA256";

    private final Clock clock;

    public SepayWebhookVerifier() {
        this(Clock.systemUTC());
    }

    SepayWebhookVerifier(Clock clock) {
        this.clock = clock;
    }

    public boolean isValid(String body, String signature, String timestampHeader, String secret) {
        if (isBlank(body) || isBlank(signature) || isBlank(timestampHeader) || isBlank(secret)) return false;
        try {
            long timestamp = Long.parseLong(timestampHeader);
            if (Math.abs(Instant.now(clock).getEpochSecond() - timestamp) > MAX_TIMESTAMP_DRIFT_SECONDS) {
                return false;
            }
            Mac mac = Mac.getInstance(HMAC_SHA_256);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA_256));
            byte[] hash = mac.doFinal((timestampHeader + "." + body).getBytes(StandardCharsets.UTF_8));
            String expected = "sha256=" + HexFormat.of().formatHex(hash);
            return MessageDigest.isEqual(
                    expected.getBytes(StandardCharsets.UTF_8),
                    signature.getBytes(StandardCharsets.UTF_8)
            );
        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

