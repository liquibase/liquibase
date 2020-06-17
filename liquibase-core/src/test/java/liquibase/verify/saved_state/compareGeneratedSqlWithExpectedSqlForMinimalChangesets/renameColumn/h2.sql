-- Database: h2
-- Change Parameter: newColumnName=full_name
-- Change Parameter: oldColumnName=name
-- Change Parameter: tableName=person
ALTER TABLE person ALTER COLUMN name RENAME TO full_name;
