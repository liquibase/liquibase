-- Database: mssql
-- Change Parameter: columnName=fileName
-- Change Parameter: defaultValue=Something Else
-- Change Parameter: tableName=file
ALTER TABLE file ADD CONSTRAINT DF_file_fileName DEFAULT 'Something Else' FOR fileName;
