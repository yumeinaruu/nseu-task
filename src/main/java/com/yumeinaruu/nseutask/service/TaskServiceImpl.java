package com.yumeinaruu.nseutask.service;

import com.yumeinaruu.nseutask.dto.TaskCreateRequest;
import com.yumeinaruu.nseutask.dto.TaskFilter;
import com.yumeinaruu.nseutask.dto.TaskResponse;
import com.yumeinaruu.nseutask.dto.TaskUpdateRequest;
import com.yumeinaruu.nseutask.entity.Task;
import com.yumeinaruu.nseutask.entity.User;
import com.yumeinaruu.nseutask.entity.enums.TaskPriority;
import com.yumeinaruu.nseutask.entity.enums.TaskStatus;
import com.yumeinaruu.nseutask.exception.InvalidStatusTransitionException;
import com.yumeinaruu.nseutask.exception.TaskConcurrentModificationException;
import com.yumeinaruu.nseutask.exception.TaskNotFoundException;
import com.yumeinaruu.nseutask.exception.UserNotFoundException;
import com.yumeinaruu.nseutask.mapper.TaskMapper;
import com.yumeinaruu.nseutask.repository.TaskRepository;
import com.yumeinaruu.nseutask.repository.TaskSpecifications;
import com.yumeinaruu.nseutask.repository.UserRepository;
import com.yumeinaruu.nseutask.security.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class TaskServiceImpl implements TaskService {

    private static final TaskStatus DEFAULT_STATUS = TaskStatus.TODO;
    private static final TaskPriority DEFAULT_PRIORITY = TaskPriority.MEDIUM;

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;
    private final TaskAccessPolicy taskAccessPolicy;

    public TaskServiceImpl(
            TaskRepository taskRepository,
            UserRepository userRepository,
            TaskMapper taskMapper,
            TaskAccessPolicy taskAccessPolicy
    ) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.taskMapper = taskMapper;
        this.taskAccessPolicy = taskAccessPolicy;
    }

    @Override
    @Transactional
    public TaskResponse createTask(TaskCreateRequest request, UserPrincipal principal) {
        log.info("Starting task creation: authorId={}, assigneeId={}", principal.getId(), request.assigneeId());
        Task task = new Task();
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setStatus(DEFAULT_STATUS);
        task.setPriority(request.priority() == null ? DEFAULT_PRIORITY : request.priority());
        task.setAuthor(findUser(principal.getId()));
        task.setAssignee(findAssignee(request.assigneeId()));
        Task savedTask = taskRepository.saveAndFlush(task);
        log.info("Completed task creation: taskId={}", savedTask.getId());
        return taskMapper.toTaskResponse(savedTask);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponse findTask(Long taskId) {
        return taskMapper.toTaskResponse(findExistingTask(taskId));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskResponse> findTasks(TaskFilter filter, Pageable pageable) {
        return taskRepository.findAll(TaskSpecifications.fromFilter(filter), pageable)
                .map(taskMapper::toTaskResponse);
    }

    @Override
    @Transactional
    public TaskResponse updateTask(Long taskId, TaskUpdateRequest request, UserPrincipal principal) {
        log.info("Starting task update: taskId={}, userId={}", taskId, principal.getId());
        Task task = findExistingTask(taskId);
        taskAccessPolicy.checkCanModify(task, principal);
        if (!task.getVersion().equals(request.version())) {
            throw new TaskConcurrentModificationException(taskId);
        }
        if (!task.getStatus().canTransitionTo(request.status())) {
            throw new InvalidStatusTransitionException(taskId, task.getStatus(), request.status());
        }
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setStatus(request.status());
        task.setPriority(request.priority());
        task.setAssignee(findAssignee(request.assigneeId()));
        Task savedTask = taskRepository.saveAndFlush(task);
        log.info("Completed task update: taskId={}, version={}", taskId, savedTask.getVersion());
        return taskMapper.toTaskResponse(savedTask);
    }

    @Override
    @Transactional
    public void deleteTask(Long taskId, UserPrincipal principal) {
        log.info("Starting task deletion: taskId={}, userId={}", taskId, principal.getId());
        Task task = findExistingTask(taskId);
        taskAccessPolicy.checkCanModify(task, principal);
        taskRepository.delete(task);
        log.info("Completed task deletion: taskId={}", taskId);
    }

    private Task findExistingTask(Long taskId) {
        return taskRepository.findById(taskId).orElseThrow(() -> new TaskNotFoundException(taskId));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
    }

    private User findAssignee(Long assigneeId) {
        return assigneeId == null ? null : findUser(assigneeId);
    }
}
