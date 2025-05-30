--liquibase formatted sql

--changeset mallodTest:1 labels: testLabel context: textContext dbms:   runWith:
create table included_sql (
                              id int not null primary key,
                              name varchar(255)
);