package vn.tera.learn.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreatePlusOrderRequest {

    @NotBlank(message = "Vui lòng chọn gói Tera Plus")
    private String planCode;
}
