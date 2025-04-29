-- Database: mysql
-- Change Parameter: columnDataType=int
-- Change Parameter: newColumnName=full_name
-- Change Parameter: oldColumnName=name
-- Change Parameter: tableName=person
ALTER TABLE person RENAME COLUMN name TO full_name;
