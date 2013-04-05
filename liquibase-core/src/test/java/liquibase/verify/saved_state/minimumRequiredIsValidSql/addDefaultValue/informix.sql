-- Database: informix
-- Change Parameter: columnDataType=int
-- Database: informix
-- Change Parameter: columnName=fileName
-- Database: informix
-- Change Parameter: defaultValue=Something Else
-- Database: informix
-- Change Parameter: tableName=file
ALTER TABLE file MODIFY (fileName int DEFAULT 'Something Else');
