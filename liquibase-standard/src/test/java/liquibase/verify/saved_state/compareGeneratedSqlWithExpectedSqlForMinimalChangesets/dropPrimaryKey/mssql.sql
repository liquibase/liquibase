-- Database: mssql
-- Change Parameter: tableName=person
DECLARE @sql [nvarchar](MAX)
SELECT @sql = N'ALTER TABLE person DROP CONSTRAINT ' + QUOTENAME([kc].[name]) FROM [sys].[key_constraints] AS [kc] WHERE [kc].[parent_object_id] = OBJECT_ID(N'person') AND [kc].[type] = 'PK'
EXEC sp_executesql @sql;
