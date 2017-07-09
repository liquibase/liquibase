-- Database: postgresql
-- Change Parameter: columnName=id
-- Change Parameter: newDataType=int
-- Change Parameter: tableName=person
ALTER TABLE person ALTER COLUMN id TYPE INTEGER USING (id::INTEGER);
