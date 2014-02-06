-- Database: db2
-- Change Parameter: newTableName=person
-- Change Parameter: oldTableName=person
RENAME person TO person;
CALL SYSPROC.ADMIN_CMD ('REORG TABLE person');
