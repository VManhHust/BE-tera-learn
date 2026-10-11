package vn.tera.learn.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StartLearningSessionRequest {
    @NotNull(message = "Vui lòng chọn chế độ học")
    private Long modeId;
}
