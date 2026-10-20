package com.yumeinaruu.nseutask.dto;

import com.yumeinaruu.nseutask.entity.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Registered user; the password is never returned")
public record UserResponse(
        @Schema(description = "User id", example = "2")
        Long id,

        @Schema(description = "Login", example = "stasik")
        String username,

        @Schema(description = "Email address", example = "stasik@example.com")
        String email,

        @Schema(description = "Role; new users always get USER")
        Role role
) {
}
