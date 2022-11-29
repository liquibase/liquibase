-- liquibase formatted sql

-- changeset flautert:3324_1
    CREATE TABLE rollback_test (
    id INT NOT NULL
)
-- rollback DROP TABLE rollback_test

-- changeset flautert:3324_2
INSERT INTO rollback_test(id) values (1)
-- rollback empty
