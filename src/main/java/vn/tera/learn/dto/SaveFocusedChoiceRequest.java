package vn.tera.learn.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public class SaveFocusedChoiceRequest {
    @NotNull
    @Size(max = 100)
    private List<@NotNull Long> optionIds;
    public List<Long> getOptionIds() { return optionIds; }
    public void setOptionIds(List<Long> optionIds) { this.optionIds = optionIds; }
}
