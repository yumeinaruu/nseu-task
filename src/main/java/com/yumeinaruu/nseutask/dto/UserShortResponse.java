package com.yumeinaruu.nseutask.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Short user info used inside a task")
public record UserShortResponse(
        @Schema(description = "User id", example = "2")
        Long id,

        @Schema(description = "Login", example = "stasik")
        String username
) {
}
