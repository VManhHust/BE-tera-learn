package vn.tera.learn.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class VerifyEmailRequest {
    @NotBlank(message = "Vui lòng nhập email")
    @Email(message = "Email không hợp lệ")
    @Size(max = 255, message = "Email quá dài")
    private String email;

    @NotBlank(message = "Vui lòng nhập mã xác thực")
    @Pattern(regexp = "\\d{4}", message = "Mã xác thực phải gồm 4 chữ số")
    private String code;

    @Override
    public String toString() {
        return "VerifyEmailRequest[email=" + email + ", code=***]";
    }
}
