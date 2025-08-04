 -- liquibase formatted sql

--changeset asmith:01 labels:POCEnv

create table example_table_04 
(
id varchar(32),
name  varchar(40),
location varchar(10)
);

--changeset Liquibase User:3
--preconditions onFail:CONTINUE
--precondition-sql-check expectedResult:1 SELECT COUNT(*) FROM example_table_04
alter table example_table_04
    add col2 varchar(3)
