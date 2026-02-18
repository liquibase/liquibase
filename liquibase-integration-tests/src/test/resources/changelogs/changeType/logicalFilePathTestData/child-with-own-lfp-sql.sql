--liquibase formatted sql logicalFilePath:child-own-logical-path

--changeset testauthor:sql_child_own_test
create table test_table_7388_own_lfp (id int primary key);
--rollback drop table test_table_7388_own_lfp;
