package com.yumeinaruu.nseutask.repository;

import com.yumeinaruu.nseutask.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    @Override
    @EntityGraph(attributePaths = {"author", "assignee"})
    Page<Task> findAll(Specification<Task> specification, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"author", "assignee"})
    Optional<Task> findById(Long id);
}
