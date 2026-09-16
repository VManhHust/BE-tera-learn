package vn.tera.learn.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class FreeTrialAnswerRequest {

    @NotEmpty(message = "Vui lòng chọn ít nhất một phương án")
    private List<@NotNull(message = "Phương án đã chọn không hợp lệ") Long> optionIds;

    public FreeTrialAnswerRequest() {
    }

    public List<Long> getOptionIds() {
        return optionIds;
    }

    public void setOptionIds(List<Long> optionIds) {
        this.optionIds = optionIds;
    }
}
