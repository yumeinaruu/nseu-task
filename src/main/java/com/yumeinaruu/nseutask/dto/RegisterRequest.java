package com.yumeinaruu.nseutask.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Data for registering a new user")
public record RegisterRequest(
        @Schema(description = "Unique login", example = "stasik")
        @NotBlank
        @Size(max = 255)
        String username,

        @Schema(description = "Unique email address", example = "stasik@example.com")
        @NotBlank
        @Email
        @Size(max = 255)
        String email,

        @Schema(description = "Password, stored as a BCrypt hash", example = "password123")
        @NotBlank
        @Size(min = 8, max = 20)
        String password
) {
}
