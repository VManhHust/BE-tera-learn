package vn.tera.learn.dto;

import java.time.Instant;

public record PlusStatusResponse(
        boolean plus,
        String currentPlanCode,
        String currentPlanName,
        Instant plusStartsAt,
        Instant plusExpiresAt
) {}

