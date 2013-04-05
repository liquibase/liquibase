-- Database: mssql
-- Change Parameter: columnName=fileName
-- Database: mssql
-- Change Parameter: defaultValue=Something Else
-- Database: mssql
-- Change Parameter: tableName=file
ALTER TABLE [file] ADD CONSTRAINT DF_file_fileName DEFAULT 'Something Else' FOR fileName;
