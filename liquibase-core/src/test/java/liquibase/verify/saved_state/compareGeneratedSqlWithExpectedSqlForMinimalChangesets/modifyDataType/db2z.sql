-- Database: db2z
-- Change Parameter: columnName=id
-- Change Parameter: newDataType=int
-- Change Parameter: tableName=person
ALTER TABLE person ALTER COLUMN id SET DATA TYPE INTEGER;
CALL SYSPROC.ADMIN_CMD ('REORG TABLE person');
