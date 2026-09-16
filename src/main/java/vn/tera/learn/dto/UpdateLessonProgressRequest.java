package vn.tera.learn.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class UpdateLessonProgressRequest {

    @NotNull(message = "Số bài hiện tại là bắt buộc")
    @Min(value = 0, message = "Số bài hiện tại không được âm")
    private Short currentUnit;

    public UpdateLessonProgressRequest() {
    }

    public Short getCurrentUnit() {
        return currentUnit;
    }

    public void setCurrentUnit(Short currentUnit) {
        this.currentUnit = currentUnit;
    }
}
