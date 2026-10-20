package com.yumeinaruu.nseutask.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Login credentials")
public record LoginRequest(
        @Schema(description = "Login of a registered user", example = "stasik")
        @NotBlank
        String username,

        @Schema(description = "User password", example = "password123")
        @NotBlank
        String password
) {
}
