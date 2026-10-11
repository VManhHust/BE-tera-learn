package vn.tera.learn.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class SubmitChillAnswerRequest {
    @NotEmpty(message = "Vui lòng chọn ít nhất một phương án")
    private List<Long> optionIds;
}
