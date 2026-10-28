--liquibase formatted sql

--changeset testauthor:1
create table test_table_7388_child (id int primary key);
--rollback drop table test_table_7388_child;
