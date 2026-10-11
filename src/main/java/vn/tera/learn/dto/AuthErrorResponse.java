package vn.tera.learn.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AuthErrorResponse {
    private String error;
    private String code;
    private Long retryAfter;
}
