package vn.tera.learn.exception;

import org.springframework.http.HttpStatus;

public class AuthFlowException extends RuntimeException {

    private final HttpStatus status;
    private final String code;
    private final Long retryAfter;

    public AuthFlowException(HttpStatus status, String code, String message) {
        this(status, code, message, null);
    }

    public AuthFlowException(HttpStatus status, String code, String message, Long retryAfter) {
        super(message);
        this.status = status;
        this.code = code;
        this.retryAfter = retryAfter;
    }

    public HttpStatus getStatus() { return status; }
    public String getCode() { return code; }
    public Long getRetryAfter() { return retryAfter; }
}
