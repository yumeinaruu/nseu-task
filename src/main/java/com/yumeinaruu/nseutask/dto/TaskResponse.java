package com.yumeinaruu.nseutask.dto;

import com.yumeinaruu.nseutask.entity.enums.TaskPriority;
import com.yumeinaruu.nseutask.entity.enums.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Task")
public record TaskResponse(
        @Schema(description = "Task id", example = "1")
        Long id,

        @Schema(description = "Title", example = "Write documentation")
        String title,

        @Schema(description = "Description", example = "Description example", nullable = true)
        String description,

        @Schema(description = "Status")
        TaskStatus status,

        @Schema(description = "Priority")
        TaskPriority priority,

        @Schema(description = "Author of the task")
        UserShortResponse author,

        @Schema(description = "Assignee, null when nobody is assigned", nullable = true)
        UserShortResponse assignee,

        @Schema(description = "Version for optimistic locking; send it back in PUT", example = "0")
        Long version,

        @Schema(description = "Creation time (UTC)", example = "2026-10-19T21:18:50.000108Z")
        Instant createdAt,

        @Schema(description = "Last update time (UTC)", example = "2026-10-19T21:18:50.079494Z")
        Instant updatedAt
) {
}
