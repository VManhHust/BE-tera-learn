package vn.tera.learn.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class FreeTrialAnswerRequest {
    @NotEmpty(message = "Vui lòng chọn ít nhất một phương án")
    private List<@NotNull(message = "Phương án đã chọn không hợp lệ") Long> optionIds;
}
