package com.yumeinaruu.nseutask.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Error returned for every failed request; the message is fixed per error code")
public record ErrorResponse(
        @Schema(description = "When the error happened (UTC)", example = "2026-10-19T21:18:50.102Z")
        Instant timestamp,

        @Schema(description = "HTTP status", example = "409")
        int status,

        @Schema(description = "Application error code: 1xxx user, 2xxx task, 3xxx general", example = "2003")
        int code,

        @Schema(description = "Client-safe description of the error", example = "Task was modified by someone else")
        String message,

        @Schema(description = "Requested path", example = "/api/tasks/1")
        String path
) {
}
