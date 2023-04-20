-- Database: db2
-- Change Parameter: columnName=id
-- Change Parameter: tableName=person
ALTER TABLE person ALTER COLUMN  id SET NOT NULL;
CALL SYSPROC.ADMIN_CMD ('REORG TABLE person');
CALL SYSPROC.ADMIN_CMD ('REORG TABLE person');
