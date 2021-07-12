--liquibase formatted sql logicalFilePath:test-path

--changeset test-author:multi-rollback runInTransaction:false
--rollback drop table dual;

