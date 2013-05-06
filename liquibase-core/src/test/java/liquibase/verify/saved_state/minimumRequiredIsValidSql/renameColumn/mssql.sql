-- Database: mssql
-- Change Parameter: newColumnName=id
-- Change Parameter: oldColumnName=id
-- Change Parameter: tableName=person
exec sp_rename '[person].[id]', 'id';
