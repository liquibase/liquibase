--liquibase formatted sql
--changeset bob:1
CREATE TABLE sampl_main.persons (
    ID INT PRIMARY KEY,
    FIRST_NAME VARCHAR(128) NOT NULL,
    LAST_NAME VARCHAR(128) NOT NULL
);

GRANT SELECT, INSERT, UPDATE ON sampl_main.persons TO "sampl_admin";

INSERT INTO sampl_main.persons (ID, FIRST_NAME, LAST_NAME) VALUES (1, 'Nathan', 'Voxland');
INSERT INTO sampl_main.persons (ID, FIRST_NAME, LAST_NAME) VALUES (2, 'Bob', 'Bobson');
INSERT INTO sampl_main.persons (ID, FIRST_NAME, LAST_NAME) VALUES (3, 'Andrei', 'Nasonov');
