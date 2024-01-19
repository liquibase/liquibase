-- Database: db2
-- Change Parameter: columnName=id
-- Change Parameter: tableName=person
ALTER TABLE person DROP COLUMN id;
CALL SYSPROC.ADMIN_CMD ('REORG TABLE person');
