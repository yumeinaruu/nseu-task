package com.yumeinaruu.nseutask.controller;

import com.yumeinaruu.nseutask.config.OpenApiConfig;
import com.yumeinaruu.nseutask.docs.ApiTaskForbiddenError;
import com.yumeinaruu.nseutask.docs.ApiTaskNotFoundError;
import com.yumeinaruu.nseutask.docs.ApiUnauthorizedError;
import com.yumeinaruu.nseutask.docs.ApiValidationError;
import com.yumeinaruu.nseutask.dto.ErrorResponse;
import com.yumeinaruu.nseutask.dto.TaskCreateRequest;
import com.yumeinaruu.nseutask.dto.TaskFilter;
import com.yumeinaruu.nseutask.dto.TaskResponse;
import com.yumeinaruu.nseutask.dto.TaskUpdateRequest;
import com.yumeinaruu.nseutask.security.UserPrincipal;
import com.yumeinaruu.nseutask.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
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

@Tag(name = "Tasks", description = "Task management. All endpoints require a JWT.")
@SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME)
@ApiUnauthorizedError
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private static final String CREATED_AT_FIELD = "createdAt";

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @Operation(
            summary = "Create a task",
            description = "The author is the current user and the status starts as TODO. "
                    + "The Location header points to the new task."
    )
    @ApiResponse(responseCode = "201", description = "Task created")
    @ApiResponse(
            responseCode = "404",
            description = "Assignee not found (code 1001)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    @ApiValidationError
    @PostMapping
    public ResponseEntity<TaskResponse> createTask(
            @Valid @RequestBody TaskCreateRequest request,
            @AuthenticationPrincipal @Parameter(hidden = true) UserPrincipal principal
    ) {
        TaskResponse response = taskService.createTask(request, principal);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @Operation(
            summary = "List tasks",
            description = "Any authenticated user sees all tasks. Filters are optional and combined with AND. "
                    + "Sorted by creation time, newest first, unless 'sort' is given "
                    + "(for example sort=priority,desc). Page size is 10 by default and at most 100."
    )
    @ApiResponse(responseCode = "200", description = "Page of tasks")
    @ApiValidationError
    @GetMapping
    public PagedModel<TaskResponse> findTasks(
            @ParameterObject TaskFilter filter,
            @ParameterObject
            @PageableDefault(sort = CREATED_AT_FIELD, direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return new PagedModel<>(taskService.findTasks(filter, pageable));
    }

    @Operation(summary = "Get a task by id")
    @ApiResponse(responseCode = "200", description = "Task found")
    @ApiTaskNotFoundError
    @GetMapping("/{taskId}")
    public TaskResponse findTask(@Parameter(description = "Task id", example = "1") @PathVariable Long taskId) {
        return taskService.findTask(taskId);
    }

    @Operation(
            summary = "Update a task",
            description = "Only the author or an admin can update a task. The body replaces all task fields: "
                    + "a null assigneeId removes the assignee. 'version' must equal the version from the last "
                    + "response, otherwise the task was changed by someone else and the answer is 409 "
                    + "(code 2003). The status DONE cannot go back to TODO, move it through IN_PROGRESS "
                    + "(409, code 2004)."
    )
    @ApiResponse(responseCode = "200", description = "Task updated, the response contains the new version")
    @ApiResponse(
            responseCode = "404",
            description = "Task (code 2001) or assignee (code 1001) not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    @ApiResponse(
            responseCode = "409",
            description = "Stale version (code 2003) or forbidden status transition (code 2004)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
    )
    @ApiTaskForbiddenError
    @ApiValidationError
    @PutMapping("/{taskId}")
    public TaskResponse updateTask(
            @Parameter(description = "Task id", example = "1") @PathVariable Long taskId,
            @Valid @RequestBody TaskUpdateRequest request,
            @AuthenticationPrincipal @Parameter(hidden = true) UserPrincipal principal
    ) {
        return taskService.updateTask(taskId, request, principal);
    }

    @Operation(summary = "Delete a task", description = "Only the author or an admin can delete a task.")
    @ApiResponse(responseCode = "204", description = "Task deleted")
    @ApiTaskForbiddenError
    @ApiTaskNotFoundError
    @DeleteMapping("/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(
            @Parameter(description = "Task id", example = "1") @PathVariable Long taskId,
            @AuthenticationPrincipal @Parameter(hidden = true) UserPrincipal principal
    ) {
        taskService.deleteTask(taskId, principal);
    }

}
