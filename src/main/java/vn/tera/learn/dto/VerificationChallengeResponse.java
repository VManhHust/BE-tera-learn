package vn.tera.learn.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VerificationChallengeResponse {
    private long expiresIn;
    private long resendAfter;
}
