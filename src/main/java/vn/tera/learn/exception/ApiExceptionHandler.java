package vn.tera.learn.exception;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import vn.tera.learn.dto.AuthErrorResponse;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(AuthFlowException.class)
    public ResponseEntity<AuthErrorResponse> handleAuthFlow(AuthFlowException exception) {
        return ResponseEntity.status(exception.getStatus())
                .body(new AuthErrorResponse(exception.getMessage(), exception.getCode(), exception.getRetryAfter()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<AuthErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .orElse("Dữ liệu không hợp lệ");
        return ResponseEntity.badRequest().body(new AuthErrorResponse(message, "VALIDATION_ERROR", null));
    }
}
