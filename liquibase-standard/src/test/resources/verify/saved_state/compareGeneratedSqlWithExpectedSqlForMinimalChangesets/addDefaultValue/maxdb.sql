-- Database: maxdb
-- Change Parameter: columnName=fileName
-- Change Parameter: defaultValue=Something Else
-- Change Parameter: tableName=file
ALTER TABLE file COLUMN  fileName ADD DEFAULT 'Something Else';
