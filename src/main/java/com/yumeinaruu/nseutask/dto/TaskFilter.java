package com.yumeinaruu.nseutask.dto;

import com.yumeinaruu.nseutask.entity.enums.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Optional filters, combined with AND")
public record TaskFilter(
        @Schema(description = "Only tasks with this status")
        TaskStatus status,

        @Schema(description = "Only tasks assigned to this user id", example = "1")
        Long assigneeId,

        @Schema(description = "Only tasks created by this user id", example = "2")
        Long authorId
) {
}
