-- liquibase formatted sql

-- changeset stasiklisovoj:1792435198090-1
CREATE SEQUENCE IF NOT EXISTS users_seq START WITH 1 INCREMENT BY 50;

-- changeset stasiklisovoj:1792435198090-2
CREATE TABLE users
(
    id         BIGINT                      NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    username   VARCHAR(255)                NOT NULL,
    email      VARCHAR(255)                NOT NULL,
    password   VARCHAR(255)                NOT NULL,
    role       VARCHAR(20)                 NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

-- changeset stasiklisovoj:1792435198090-3
ALTER TABLE users
    ADD CONSTRAINT uc_users_email UNIQUE (email);

-- changeset stasiklisovoj:1792435198090-4
ALTER TABLE users
    ADD CONSTRAINT uc_users_username UNIQUE (username);

