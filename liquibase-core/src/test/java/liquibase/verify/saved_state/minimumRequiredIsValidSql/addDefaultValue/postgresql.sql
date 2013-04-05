-- Database: postgresql
-- Change Parameter: columnName=fileName
-- Database: postgresql
-- Change Parameter: defaultValue=Something Else
-- Database: postgresql
-- Change Parameter: tableName=file
ALTER TABLE file ALTER COLUMN  "fileName" SET DEFAULT 'Something Else';
