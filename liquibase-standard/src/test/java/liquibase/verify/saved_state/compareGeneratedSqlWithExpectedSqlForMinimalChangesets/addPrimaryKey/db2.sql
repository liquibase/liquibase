-- Database: db2
-- Change Parameter: columnNames=id, name
-- Change Parameter: tableName=person
ALTER TABLE person ADD PRIMARY KEY (id, name);
CALL SYSPROC.ADMIN_CMD ('REORG TABLE person');
