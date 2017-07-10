-- Database: mssql
-- Change Parameter: columns=[column:[
--     name="id"
--     type="int"
-- ], ]
-- Change Parameter: tableName=person
CREATE NONCLUSTERED INDEX ON person(id);
