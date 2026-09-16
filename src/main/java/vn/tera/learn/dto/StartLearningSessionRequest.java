package vn.tera.learn.dto;

import jakarta.validation.constraints.NotNull;

public class StartLearningSessionRequest {

    @NotNull(message = "Vui lòng chọn chế độ học")
    private Long modeId;

    public StartLearningSessionRequest() {
    }

    public Long getModeId() { return modeId; }
    public void setModeId(Long modeId) { this.modeId = modeId; }
}
