-- Database: db2
-- Change Parameter: newTableName=employee
-- Change Parameter: oldTableName=person
RENAME person TO employee;
CALL SYSPROC.ADMIN_CMD ('REORG TABLE employee');
