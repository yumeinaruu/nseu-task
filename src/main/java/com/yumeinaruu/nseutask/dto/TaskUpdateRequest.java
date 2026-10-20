package com.yumeinaruu.nseutask.dto;

import com.yumeinaruu.nseutask.entity.enums.TaskPriority;
import com.yumeinaruu.nseutask.entity.enums.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TaskUpdateRequest(
        @NotBlank
        @Size(max = 255)
        String title,

        @Size(max = 4000)
        String description,

        @NotNull
        TaskStatus status,

        @NotNull
        TaskPriority priority,

        Long assigneeId,

        @NotNull
        Long version
) {
}
