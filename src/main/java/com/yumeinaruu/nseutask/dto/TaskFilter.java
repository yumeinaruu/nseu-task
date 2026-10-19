package com.yumeinaruu.nseutask.dto;

import com.yumeinaruu.nseutask.entity.enums.TaskStatus;

public record TaskFilter(
        TaskStatus status,
        Long assigneeId,
        Long authorId
) {
}
