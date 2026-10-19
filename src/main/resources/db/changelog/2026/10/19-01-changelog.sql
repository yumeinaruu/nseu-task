-- liquibase formatted sql

-- changeset stasiklisovoj:1792434270233-1
CREATE SEQUENCE IF NOT EXISTS tasks_seq START WITH 1 INCREMENT BY 50;

-- changeset stasiklisovoj:1792434270233-2
CREATE TABLE tasks
(
    id          BIGINT                      NOT NULL,
    created_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    title       VARCHAR(255)                NOT NULL,
    description VARCHAR(4000),
    status      VARCHAR(20)                 NOT NULL,
    priority    VARCHAR(20)                 NOT NULL,
    author_id   BIGINT                      NOT NULL,
    assignee_id BIGINT,
    CONSTRAINT pk_tasks PRIMARY KEY (id)
);

-- changeset stasiklisovoj:1792434270233-3
ALTER TABLE tasks
    ADD CONSTRAINT FK_TASKS_ON_ASSIGNEE FOREIGN KEY (assignee_id) REFERENCES users (id);

-- changeset stasiklisovoj:1792434270233-4
ALTER TABLE tasks
    ADD CONSTRAINT FK_TASKS_ON_AUTHOR FOREIGN KEY (author_id) REFERENCES users (id);

