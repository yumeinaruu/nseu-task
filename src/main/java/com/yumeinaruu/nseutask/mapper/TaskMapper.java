package com.yumeinaruu.nseutask.mapper;

import com.yumeinaruu.nseutask.dto.TaskResponse;
import com.yumeinaruu.nseutask.entity.Task;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {

    private final UserMapper userMapper;

    public TaskMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public TaskResponse toTaskResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                userMapper.toUserShortResponse(task.getAuthor()),
                task.getAssignee() == null ? null : userMapper.toUserShortResponse(task.getAssignee()),
                task.getVersion(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }

}
