package com.yumeinaruu.nseutask.dto;

import com.yumeinaruu.nseutask.entity.enums.TaskPriority;
import com.yumeinaruu.nseutask.entity.enums.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Full replacement of the task fields; empty optional fields are cleared")
public record TaskUpdateRequest(
        @Schema(description = "Task title", example = "Write documentation")
        @NotBlank
        @Size(max = 255)
        String title,

        @Schema(description = "Task description", example = "Description example")
        @Size(max = 4000)
        String description,

        @Schema(description = "New status; DONE cannot go back to TODO", example = "IN_PROGRESS")
        @NotNull
        TaskStatus status,

        @Schema(description = "Priority", example = "HIGH")
        @NotNull
        TaskPriority priority,

        @Schema(description = "Id of the assignee; null removes the assignee", example = "1")
        Long assigneeId,

        @Schema(description = "Version from the last response for this task; protects from overwriting "
                + "someone else's changes (409, code 2003, when it is stale)", example = "0")
        @NotNull
        Long version
) {
}
