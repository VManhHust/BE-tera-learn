package vn.tera.learn.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class SaveFocusedChoiceRequest {
    @NotNull
    @Size(max = 100)
    private List<@NotNull Long> optionIds;
}
