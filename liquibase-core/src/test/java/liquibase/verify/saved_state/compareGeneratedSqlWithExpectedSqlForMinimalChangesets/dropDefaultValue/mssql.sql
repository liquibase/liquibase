-- Database: mssql
-- Change Parameter: columnName=id
-- Change Parameter: tableName=person
DECLARE @sql [nvarchar](MAX)
SELECT @sql = N'ALTER TABLE person DROP CONSTRAINT ' + QUOTENAME([df].[name]) FROM [sys].[columns] AS [c] INNER JOIN [sys].[default_constraints] AS [df] ON [df].[object_id] = [c].[default_object_id] WHERE [c].[object_id] = OBJECT_ID(N'person') AND [c].[name] = N'id'
EXEC sp_executesql @sql;
