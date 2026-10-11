package vn.tera.learn.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UpdateLessonProgressRequest {
    @NotNull(message = "Số bài hiện tại là bắt buộc")
    @Min(value = 0, message = "Số bài hiện tại không được âm")
    private Short currentUnit;
}
