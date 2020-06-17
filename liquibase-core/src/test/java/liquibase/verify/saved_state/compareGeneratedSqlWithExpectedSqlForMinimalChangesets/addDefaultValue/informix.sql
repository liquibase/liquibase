-- Database: informix
-- Change Parameter: columnDataType=int
-- Change Parameter: columnName=fileName
-- Change Parameter: defaultValue=Something Else
-- Change Parameter: tableName=file
ALTER TABLE file MODIFY (fileName int DEFAULT 'Something Else');
