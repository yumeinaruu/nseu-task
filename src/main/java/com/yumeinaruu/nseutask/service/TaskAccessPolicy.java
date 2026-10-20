package com.yumeinaruu.nseutask.service;

import com.yumeinaruu.nseutask.entity.Task;
import com.yumeinaruu.nseutask.entity.enums.Role;
import com.yumeinaruu.nseutask.exception.TaskAccessDeniedException;
import com.yumeinaruu.nseutask.security.UserPrincipal;
import org.springframework.stereotype.Component;

@Component
public class TaskAccessPolicy {

    public void checkCanModify(Task task, UserPrincipal principal) {
        boolean isAuthor = task.getAuthor().getId().equals(principal.getId());
        if (!isAuthor && principal.getRole() != Role.ADMIN) {
            throw new TaskAccessDeniedException(task.getId());
        }
    }
}
