-- Database: mssql
-- Change Parameter: columnName=id
-- Database: mssql
-- Change Parameter: tableName=person
DECLARE @default sysname
SELECT @default = object_name(default_object_id) FROM sys.columns WHERE object_id=object_id('null.person') AND name='id'
EXEC ('ALTER TABLE [person] DROP CONSTRAINT ' + @default);
