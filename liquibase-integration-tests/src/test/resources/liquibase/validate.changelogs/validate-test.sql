--liquibase formatted sql

--changeset mallodTest:1 labels: context: dbms:  runWith: logicalFilePath:
create table included_sql (
                              id int not null primary key,
                              name varchar(255)
);