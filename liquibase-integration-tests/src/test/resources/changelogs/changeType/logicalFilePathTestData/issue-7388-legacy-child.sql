--liquibase formatted sql

--changeset testauthor:sql_legacy_child1
create table test_table_7388_legacy (id int primary key);
--rollback drop table test_table_7388_legacy;
