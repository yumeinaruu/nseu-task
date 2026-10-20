-- liquibase formatted sql

-- changeset stasiklisovoj:1797434270233-8
ALTER TABLE tasks
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
