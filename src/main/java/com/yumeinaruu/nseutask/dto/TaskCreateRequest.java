package com.yumeinaruu.nseutask.dto;

import com.yumeinaruu.nseutask.entity.enums.TaskPriority;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Data for creating a task; the author is the current user and the status starts as TODO")
public record TaskCreateRequest(
        @Schema(description = "Task title", example = "Write documentation")
        @NotBlank
        @Size(max = 255)
        String title,

        @Schema(description = "Task description", example = "Example description")
        @Size(max = 4000)
        String description,

        @Schema(description = "Priority; MEDIUM when empty", example = "HIGH")
        TaskPriority priority,

        @Schema(description = "Id of the assignee; no assignee when empty", example = "1")
        Long assigneeId
) {
}
