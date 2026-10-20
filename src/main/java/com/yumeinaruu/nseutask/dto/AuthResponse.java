package com.yumeinaruu.nseutask.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Result of a successful login")
public record AuthResponse(
        @Schema(description = "JWT for the Authorization: Bearer header; contains username, role and expiration time",
                example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huIn0.signature")
        String token
) {
}
