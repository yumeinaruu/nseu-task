package com.yumeinaruu.nseutask.entity.enums;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class TaskStatusTest {

    @ParameterizedTest
    @CsvSource({
            "TODO, TODO",
            "TODO, IN_PROGRESS",
            "TODO, DONE",
            "IN_PROGRESS, TODO",
            "IN_PROGRESS, IN_PROGRESS",
            "IN_PROGRESS, DONE",
            "DONE, IN_PROGRESS",
            "DONE, DONE",
    })
    void givenAllowedPair_whenCheckingTransition_thenReturnsTrue(TaskStatus from, TaskStatus to) {
        assertThat(from.canTransitionTo(to)).isTrue();
    }

    @ParameterizedTest
    @CsvSource({"DONE, TODO"})
    void givenForbiddenPair_whenCheckingTransition_thenReturnsFalse(TaskStatus from, TaskStatus to) {
        assertThat(from.canTransitionTo(to)).isFalse();
    }

}
