package vn.tera.learn.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FreeTrialAnswerResponse {
    private Long questionId;
    private boolean correct;
    private List<Long> selectedOptionIds;
    private List<Long> correctOptionIds;
    private String explanation;
    private String youtubeTitle;
    private String youtubeUrl;
}
