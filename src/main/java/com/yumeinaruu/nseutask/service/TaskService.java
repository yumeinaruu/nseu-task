package com.yumeinaruu.nseutask.service;

import com.yumeinaruu.nseutask.dto.TaskCreateRequest;
import com.yumeinaruu.nseutask.dto.TaskFilter;
import com.yumeinaruu.nseutask.dto.TaskResponse;
import com.yumeinaruu.nseutask.dto.TaskUpdateRequest;
import com.yumeinaruu.nseutask.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TaskService {

    TaskResponse createTask(TaskCreateRequest request, UserPrincipal principal);

    TaskResponse findTask(Long taskId);

    Page<TaskResponse> findTasks(TaskFilter filter, Pageable pageable);

    TaskResponse updateTask(Long taskId, TaskUpdateRequest request, UserPrincipal principal);

    void deleteTask(Long taskId, UserPrincipal principal);

}
