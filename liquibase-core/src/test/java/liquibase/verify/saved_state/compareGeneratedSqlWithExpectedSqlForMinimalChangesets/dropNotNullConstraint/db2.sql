-- Database: db2
-- Change Parameter: columnName=id
-- Change Parameter: tableName=person
ALTER TABLE person ALTER COLUMN  id DROP NOT NULL;
CALL SYSPROC.ADMIN_CMD ('REORG TABLE person');
