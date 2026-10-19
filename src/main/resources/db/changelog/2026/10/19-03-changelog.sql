-- liquibase formatted sql

-- changeset stasiklisovoj:1797434270233-6
CREATE INDEX idx_tasks_author_id ON tasks (author_id);

-- changeset stasiklisovoj:1797434270233-7
CREATE INDEX idx_tasks_assignee_id ON tasks (assignee_id);
