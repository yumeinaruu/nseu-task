package com.yumeinaruu.nseutask.dto;

import com.yumeinaruu.nseutask.entity.enums.TaskPriority;
import com.yumeinaruu.nseutask.entity.enums.TaskStatus;
import java.time.Instant;

public record TaskResponse(
        Long id,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        UserShortResponse author,
        UserShortResponse assignee,
        Long version,
        Instant createdAt,
        Instant updatedAt
) {
}
