package com.yumeinaruu.nseutask.dto;

import java.time.Instant;

public record ErrorResponse(
        Instant timestamp,
        int status,
        int code,
        String message,
        String path
) {
}
