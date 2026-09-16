package vn.tera.learn.dto;

import jakarta.validation.constraints.NotBlank;

public record CreatePlusOrderRequest(
        @NotBlank(message = "Vui lòng chọn gói Tera Plus") String planCode
) {}

