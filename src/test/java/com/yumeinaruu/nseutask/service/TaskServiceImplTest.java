package com.yumeinaruu.nseutask.service;

import com.yumeinaruu.nseutask.dto.TaskCreateRequest;
import com.yumeinaruu.nseutask.dto.TaskResponse;
import com.yumeinaruu.nseutask.dto.TaskUpdateRequest;
import com.yumeinaruu.nseutask.entity.Task;
import com.yumeinaruu.nseutask.entity.User;
import com.yumeinaruu.nseutask.entity.enums.Role;
import com.yumeinaruu.nseutask.entity.enums.TaskPriority;
import com.yumeinaruu.nseutask.entity.enums.TaskStatus;
import com.yumeinaruu.nseutask.exception.InvalidStatusTransitionException;
import com.yumeinaruu.nseutask.exception.TaskAccessDeniedException;
import com.yumeinaruu.nseutask.exception.TaskConcurrentModificationException;
import com.yumeinaruu.nseutask.exception.TaskNotFoundException;
import com.yumeinaruu.nseutask.exception.UserNotFoundException;
import com.yumeinaruu.nseutask.mapper.TaskMapper;
import com.yumeinaruu.nseutask.mapper.UserMapper;
import com.yumeinaruu.nseutask.repository.TaskRepository;
import com.yumeinaruu.nseutask.repository.UserRepository;
import com.yumeinaruu.nseutask.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    private static final Long AUTHOR_ID = 1L;
    private static final Long OTHER_USER_ID = 2L;
    private static final Long ASSIGNEE_ID = 3L;
    private static final Long TASK_ID = 10L;
    private static final Long VERSION = 4L;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    private TaskServiceImpl taskService;

    @BeforeEach
    void setUp() {
        taskService = new TaskServiceImpl(
                taskRepository,
                userRepository,
                new TaskMapper(new UserMapper()),
                new TaskAccessPolicy()
        );
    }

    @Test
    void givenRequestWithoutPriority_whenCreatingTask_thenAppliesDefaults() {
        when(userRepository.findById(AUTHOR_ID)).thenReturn(Optional.of(user(AUTHOR_ID, Role.USER)));
        when(taskRepository.saveAndFlush(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse response = taskService.createTask(
                new TaskCreateRequest("Title", null, null, null),
                principal(AUTHOR_ID, Role.USER)
        );

        ArgumentCaptor<Task> savedTask = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).saveAndFlush(savedTask.capture());
        assertThat(savedTask.getValue().getTitle()).isEqualTo("Title");
        assertThat(savedTask.getValue().getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(savedTask.getValue().getPriority()).isEqualTo(TaskPriority.MEDIUM);
        assertThat(savedTask.getValue().getAuthor().getId()).isEqualTo(AUTHOR_ID);
        assertThat(savedTask.getValue().getAssignee()).isNull();
        assertThat(response.status()).isEqualTo(TaskStatus.TODO);
    }

    @Test
    void givenAssignee_whenCreatingTask_thenSetsAssignee() {
        when(userRepository.findById(AUTHOR_ID)).thenReturn(Optional.of(user(AUTHOR_ID, Role.USER)));
        when(userRepository.findById(ASSIGNEE_ID)).thenReturn(Optional.of(user(ASSIGNEE_ID, Role.USER)));
        when(taskRepository.saveAndFlush(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse response = taskService.createTask(
                new TaskCreateRequest("Title", "Text", TaskPriority.HIGH, ASSIGNEE_ID),
                principal(AUTHOR_ID, Role.USER)
        );

        assertThat(response.assignee().id()).isEqualTo(ASSIGNEE_ID);
        assertThat(response.priority()).isEqualTo(TaskPriority.HIGH);
    }

    @Test
    void givenUnknownAssignee_whenCreatingTask_thenThrowsUserNotFound() {
        when(userRepository.findById(AUTHOR_ID)).thenReturn(Optional.of(user(AUTHOR_ID, Role.USER)));
        when(userRepository.findById(ASSIGNEE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.createTask(
                new TaskCreateRequest("Title", null, null, ASSIGNEE_ID),
                principal(AUTHOR_ID, Role.USER)
        )).isInstanceOf(UserNotFoundException.class);
        verify(taskRepository, never()).saveAndFlush(any());
    }

    @Test
    void givenUnknownTask_whenFindingTask_thenThrowsTaskNotFound() {
        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.findTask(TASK_ID)).isInstanceOf(TaskNotFoundException.class);
    }

    @Test
    void givenAuthorAndActualVersion_whenUpdatingTask_thenAppliesAllFields() {
        Task task = existingTask(TaskStatus.TODO);
        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));
        when(userRepository.findById(ASSIGNEE_ID)).thenReturn(Optional.of(user(ASSIGNEE_ID, Role.USER)));
        when(taskRepository.saveAndFlush(task)).thenReturn(task);

        TaskResponse response = taskService.updateTask(
                TASK_ID,
                updateRequest(TaskStatus.IN_PROGRESS, ASSIGNEE_ID, VERSION),
                principal(AUTHOR_ID, Role.USER)
        );

        ArgumentCaptor<Task> savedTask = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).saveAndFlush(savedTask.capture());
        assertThat(savedTask.getValue().getTitle()).isEqualTo("New title");
        assertThat(savedTask.getValue().getDescription()).isEqualTo("Text");
        assertThat(savedTask.getValue().getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(savedTask.getValue().getPriority()).isEqualTo(TaskPriority.HIGH);
        assertThat(savedTask.getValue().getAssignee().getId()).isEqualTo(ASSIGNEE_ID);
        assertThat(response.assignee().id()).isEqualTo(ASSIGNEE_ID);
    }

    @Test
    void givenNullAssignee_whenUpdatingTask_thenUnassignsTask() {
        Task task = existingTask(TaskStatus.TODO);
        task.setAssignee(user(ASSIGNEE_ID, Role.USER));
        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));
        when(taskRepository.saveAndFlush(task)).thenReturn(task);

        TaskResponse response = taskService.updateTask(
                TASK_ID,
                updateRequest(TaskStatus.TODO, null, VERSION),
                principal(AUTHOR_ID, Role.USER)
        );

        assertThat(response.assignee()).isNull();
    }

    @Test
    void givenStaleVersion_whenUpdatingTask_thenThrowsConcurrentModification() {
        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(existingTask(TaskStatus.TODO)));

        assertThatThrownBy(() -> taskService.updateTask(
                TASK_ID,
                updateRequest(TaskStatus.TODO, null, VERSION - 1),
                principal(AUTHOR_ID, Role.USER)
        )).isInstanceOf(TaskConcurrentModificationException.class);
        verify(taskRepository, never()).saveAndFlush(any());
    }

    @Test
    void givenDoneTask_whenMovingToTodo_thenThrowsInvalidTransition() {
        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(existingTask(TaskStatus.DONE)));

        assertThatThrownBy(() -> taskService.updateTask(
                TASK_ID,
                updateRequest(TaskStatus.TODO, null, VERSION),
                principal(AUTHOR_ID, Role.USER)
        )).isInstanceOf(InvalidStatusTransitionException.class);
        verify(taskRepository, never()).saveAndFlush(any());
    }

    @Test
    void givenForeignUser_whenUpdatingTask_thenThrowsAccessDenied() {
        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(existingTask(TaskStatus.TODO)));

        assertThatThrownBy(() -> taskService.updateTask(
                TASK_ID,
                updateRequest(TaskStatus.TODO, null, VERSION),
                principal(OTHER_USER_ID, Role.USER)
        )).isInstanceOf(TaskAccessDeniedException.class);
        verify(taskRepository, never()).saveAndFlush(any());
    }

    @Test
    void givenAuthor_whenDeletingTask_thenDeletesIt() {
        Task task = existingTask(TaskStatus.TODO);
        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(task));

        taskService.deleteTask(TASK_ID, principal(AUTHOR_ID, Role.USER));

        verify(taskRepository).delete(task);
    }

    @Test
    void givenForeignUser_whenDeletingTask_thenThrowsAccessDeniedAndKeepsTask() {
        when(taskRepository.findById(TASK_ID)).thenReturn(Optional.of(existingTask(TaskStatus.TODO)));

        assertThatThrownBy(() -> taskService.deleteTask(TASK_ID, principal(OTHER_USER_ID, Role.USER)))
                .isInstanceOf(TaskAccessDeniedException.class);
        verify(taskRepository, never()).delete(any(Task.class));
    }

    private Task existingTask(TaskStatus status) {
        Task task = new Task();
        task.setId(TASK_ID);
        task.setTitle("Old title");
        task.setStatus(status);
        task.setPriority(TaskPriority.LOW);
        task.setAuthor(user(AUTHOR_ID, Role.USER));
        task.setVersion(VERSION);
        return task;
    }

    private TaskUpdateRequest updateRequest(TaskStatus status, Long assigneeId, Long version) {
        return new TaskUpdateRequest("New title", "Text", status, TaskPriority.HIGH, assigneeId, version);
    }

    private User user(Long userId, Role role) {
        User user = new User();
        user.setId(userId);
        user.setUsername("user-" + userId);
        user.setPassword("encoded");
        user.setRole(role);
        return user;
    }

    private UserPrincipal principal(Long userId, Role role) {
        return new UserPrincipal(user(userId, role));
    }

}
