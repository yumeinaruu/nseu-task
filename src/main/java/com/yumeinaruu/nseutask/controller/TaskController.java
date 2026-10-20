package com.yumeinaruu.nseutask.controller;

import com.yumeinaruu.nseutask.dto.TaskCreateRequest;
import com.yumeinaruu.nseutask.dto.TaskFilter;
import com.yumeinaruu.nseutask.dto.TaskResponse;
import com.yumeinaruu.nseutask.dto.TaskUpdateRequest;
import com.yumeinaruu.nseutask.security.UserPrincipal;
import com.yumeinaruu.nseutask.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private static final String CREATED_AT_FIELD = "createdAt";

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(
            @Valid @RequestBody TaskCreateRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        TaskResponse response = taskService.createTask(request, principal);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public Page<TaskResponse> findTasks(
            TaskFilter filter,
            @PageableDefault(sort = CREATED_AT_FIELD, direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return taskService.findTasks(filter, pageable);
    }

    @GetMapping("/{taskId}")
    public TaskResponse findTask(@PathVariable Long taskId) {
        return taskService.findTask(taskId);
    }

    @PutMapping("/{taskId}")
    public TaskResponse updateTask(
            @PathVariable Long taskId,
            @Valid @RequestBody TaskUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return taskService.updateTask(taskId, request, principal);
    }

    @DeleteMapping("/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable Long taskId, @AuthenticationPrincipal UserPrincipal principal) {
        taskService.deleteTask(taskId, principal);
    }

}
