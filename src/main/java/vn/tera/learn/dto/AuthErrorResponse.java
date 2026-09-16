package vn.tera.learn.dto;

public class AuthErrorResponse {

    private String error;
    private String code;
    private Long retryAfter;

    public AuthErrorResponse() {
    }

    public AuthErrorResponse(String error, String code, Long retryAfter) {
        this.error = error;
        this.code = code;
        this.retryAfter = retryAfter;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getRetryAfter() {
        return retryAfter;
    }

    public void setRetryAfter(Long retryAfter) {
        this.retryAfter = retryAfter;
    }
}
