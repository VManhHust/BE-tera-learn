package vn.tera.learn.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class SubmitChillAnswerRequest {

    @NotEmpty(message = "Vui lòng chọn ít nhất một phương án")
    private List<Long> optionIds;

    public SubmitChillAnswerRequest() {
    }

    public List<Long> getOptionIds() { return optionIds; }
    public void setOptionIds(List<Long> optionIds) { this.optionIds = optionIds; }
}
