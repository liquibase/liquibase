-- Database: firebird
-- Change Parameter: columns=[column:[
--     name="id"
--     type="int"
-- ], ]
-- Change Parameter: indexName=idx_address
-- Change Parameter: tableName=person
CREATE INDEX idx_address ON person(id);
