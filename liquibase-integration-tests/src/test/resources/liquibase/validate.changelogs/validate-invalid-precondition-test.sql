--liquibase formatted sql

--changeset mallodTest:1
--precondition-test
create table included_sql (
                              id int not null primary key,
                              name varchar(255)

);