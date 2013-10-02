--liquibase formatted sql

--changeset nvoxland:1
create table test1 (
  id int not null primary key,
  name varchar(255)
);

--rollback
delete from test1;

--changeset nvoxland:2
insert into test1 (id, name) values (1, 'name 1');
insert into test1 (id, name) values (2, 'name 2');

--changeset ryan:1 context:hyphen-context-using-sql
create table hyphen_context (id integer);

--changeset ryan:2 context:camelCaseContextUsingSql
create table camel_context (id integer);

--changeset ryan:1-bar
create table bar_id (id integer);

--changeset ryan:1-foo
create table foo_id (id integer);