package com.yumeinaruu.nseutask.dto;

import com.yumeinaruu.nseutask.entity.enums.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TaskCreateRequest(
        @NotBlank
        @Size(max = 255)
        String title,

        @Size(max = 4000)
        String description,

        TaskPriority priority,

        Long assigneeId
) {
}
