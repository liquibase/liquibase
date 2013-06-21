--liquibase formatted sql

--changeset nvoxland:1
create table included_sql (
  id int not null primary key,
  name varchar(255)
);
