-- Database: db2
-- Change Parameter: columns=[column:[
--     name="id"
--     type="int"
-- ], ]
-- Change Parameter: tableName=person
ALTER TABLE person ADD id INTEGER;
CALL SYSPROC.ADMIN_CMD ('REORG TABLE person');
