--liquibase formatted sql

--changeset nvoxland:1
create table test1 (
  id int not null primary key,
  name varchar(255)
);

--changeset nvoxland:2
insert into test1 (id, name) values (1, 'name 1');
insert into test1 (id, name) values (2, 'name 2');