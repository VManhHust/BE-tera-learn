package vn.tera.learn.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
public class LearningSessionResponse {
    private Long sessionId;
    private Long lessonId;
    private Long sectionId;
    private short sectionNumber;
    private String sectionTitle;
    private Long modeId;
    private String modeCode;
    private String modeName;
    private String status;
    private Instant startedAt;
    private Instant expiresAt;
}
