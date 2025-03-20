--liquibase formatted sql

--changeset Liquibase Pro User:createPrimaryTable context:"a-new"  labels:c1,c2,new6635,new6640 
CREATE TABLE PRIMARY_TABLE_CHANGELOG_SQL (ID NCHAR(10) PRIMARY KEY);

--changeset Liquibase Pro User:createSecondaryTable context:"a-new"  labels:c1,c2,new6635,new6640 
--rollback DROP TABLE SECONDARY_TABLE; COMMIT;
CREATE TABLE SECONDARY_TABLE_CHANGELOG_SQL (ID NCHAR(10), TEXT_COLUMN VARCHAR(255), FK_COL NCHAR(10));

--changeset Liquibase Pro User:createView contextFilter:"a-new" labels:c1,c2,new6635,new6640 
--rollback DROP VIEW VIEW1; COMMIT;
CREATE VIEW VIEW1 AS SELECT ID FROM PRIMARY_TABLE_CHANGELOG_SQL;
