-- Database: mssql
-- Change Parameter: columnName=id
-- Change Parameter: tableName=person
DECLARE @default sysname
SELECT @default = object_name(default_object_id) FROM sys.columns WHERE object_id=object_id('[person]') AND name='id'
EXEC ('ALTER TABLE [person] DROP CONSTRAINT ' + @default);
