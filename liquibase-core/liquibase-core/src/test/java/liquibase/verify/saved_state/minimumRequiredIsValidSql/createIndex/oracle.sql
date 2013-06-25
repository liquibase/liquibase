-- Database: oracle
-- Change Parameter: columns=[column:[
--     name="id"
--     type="int"
-- ], ]
-- Change Parameter: tableName=user
CREATE INDEX ON user(id) parallel 3 nologging;
