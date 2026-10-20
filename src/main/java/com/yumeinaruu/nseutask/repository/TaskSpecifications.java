package com.yumeinaruu.nseutask.repository;

import com.yumeinaruu.nseutask.dto.TaskFilter;
import com.yumeinaruu.nseutask.entity.Task;
import com.yumeinaruu.nseutask.entity.enums.TaskStatus;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class TaskSpecifications {

    private static final String STATUS_FIELD = "status";
    private static final String ASSIGNEE_FIELD = "assignee";
    private static final String AUTHOR_FIELD = "author";
    private static final String ID_FIELD = "id";

    private TaskSpecifications() {
    }

    public static Specification<Task> fromFilter(TaskFilter filter) {
        List<Specification<Task>> specifications = new ArrayList<>();
        if (filter.status() != null) {
            specifications.add(hasStatus(filter.status()));
        }
        if (filter.assigneeId() != null) {
            specifications.add(hasAssigneeId(filter.assigneeId()));
        }
        if (filter.authorId() != null) {
            specifications.add(hasAuthorId(filter.authorId()));
        }
        return Specification.allOf(specifications);
    }

    private static Specification<Task> hasStatus(TaskStatus status) {
        return (root, query, builder) -> builder.equal(root.get(STATUS_FIELD), status);
    }

    private static Specification<Task> hasAssigneeId(Long assigneeId) {
        return (root, query, builder) -> builder.equal(root.get(ASSIGNEE_FIELD).get(ID_FIELD), assigneeId);
    }

    private static Specification<Task> hasAuthorId(Long authorId) {
        return (root, query, builder) -> builder.equal(root.get(AUTHOR_FIELD).get(ID_FIELD), authorId);
    }
}
