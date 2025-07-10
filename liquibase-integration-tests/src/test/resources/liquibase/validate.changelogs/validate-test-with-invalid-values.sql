--liquibase formatted sql

--changeset mallodTest:1 dbms:postgreqs runWith: testRunWith
create table included_sql (
                              id int not null primary key,
                              name varchar(255)
);