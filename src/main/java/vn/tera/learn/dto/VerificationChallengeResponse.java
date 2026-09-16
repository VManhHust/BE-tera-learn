package vn.tera.learn.dto;

public class VerificationChallengeResponse {

    private long expiresIn;
    private long resendAfter;

    public VerificationChallengeResponse() {
    }

    public VerificationChallengeResponse(long expiresIn, long resendAfter) {
        this.expiresIn = expiresIn;
        this.resendAfter = resendAfter;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public long getResendAfter() {
        return resendAfter;
    }

    public void setResendAfter(long resendAfter) {
        this.resendAfter = resendAfter;
    }
}
