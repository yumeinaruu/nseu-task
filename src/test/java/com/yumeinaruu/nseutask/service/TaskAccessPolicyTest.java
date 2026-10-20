package com.yumeinaruu.nseutask.service;

import com.yumeinaruu.nseutask.entity.Task;
import com.yumeinaruu.nseutask.entity.User;
import com.yumeinaruu.nseutask.entity.enums.Role;
import com.yumeinaruu.nseutask.exception.TaskAccessDeniedException;
import com.yumeinaruu.nseutask.security.UserPrincipal;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TaskAccessPolicyTest {

    private static final Long AUTHOR_ID = 1L;
    private static final Long OTHER_USER_ID = 2L;

    private final TaskAccessPolicy taskAccessPolicy = new TaskAccessPolicy();

    @Test
    void givenAuthor_whenCheckingModifyAccess_thenAllows() {
        Task task = taskAuthoredBy(AUTHOR_ID);

        assertThatCode(() -> taskAccessPolicy.checkCanModify(task, principal(AUTHOR_ID, Role.USER)))
                .doesNotThrowAnyException();
    }

    @Test
    void givenAdmin_whenCheckingModifyAccessToForeignTask_thenAllows() {
        Task task = taskAuthoredBy(AUTHOR_ID);

        assertThatCode(() -> taskAccessPolicy.checkCanModify(task, principal(OTHER_USER_ID, Role.ADMIN)))
                .doesNotThrowAnyException();
    }

    @Test
    void givenOrdinaryUser_whenCheckingModifyAccessToForeignTask_thenThrowsAccessDenied() {
        Task task = taskAuthoredBy(AUTHOR_ID);

        assertThatThrownBy(() -> taskAccessPolicy.checkCanModify(task, principal(OTHER_USER_ID, Role.USER)))
                .isInstanceOf(TaskAccessDeniedException.class);
    }

    private Task taskAuthoredBy(Long authorId) {
        User author = new User();
        author.setId(authorId);
        Task task = new Task();
        task.setId(10L);
        task.setAuthor(author);
        return task;
    }

    private UserPrincipal principal(Long userId, Role role) {
        User user = new User();
        user.setId(userId);
        user.setUsername("user-" + userId);
        user.setPassword("encoded");
        user.setRole(role);
        return new UserPrincipal(user);
    }

}
