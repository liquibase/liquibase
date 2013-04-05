-- Database: maxdb
-- Change Parameter: columnName=fileName
-- Database: maxdb
-- Change Parameter: defaultValue=Something Else
-- Database: maxdb
-- Change Parameter: tableName=file
ALTER TABLE file COLUMN  fileName ADD DEFAULT 'Something Else';
