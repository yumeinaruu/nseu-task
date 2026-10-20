package com.yumeinaruu.nseutask.entity.enums;

public enum TaskStatus {
    TODO,
    IN_PROGRESS,
    DONE;

    /**
     * завершенную задачу нельзя вернуть в начало, нужно прогнать через IN_PROGRESS.
     * если бы было больше статусов, можно было бы использовать спринг стейт машин,
     * для переходов
     */
    public boolean canTransitionTo(TaskStatus target) {
        return !(this == DONE && target == TODO);
    }

}
