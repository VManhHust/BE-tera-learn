package vn.tera.learn.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PlusStatusResponse {

    private boolean plus;
    private String currentPlanCode;
    private String currentPlanName;
    private Instant plusStartsAt;
    private Instant plusExpiresAt;
}
