-- Database: mssql
-- Change Parameter: column1Name=first_name
-- Change Parameter: column2Name=last_name
-- Change Parameter: finalColumnName=full_name
-- Change Parameter: finalColumnType=varchar(255)
-- Change Parameter: tableName=person
ALTER TABLE person ADD full_name varchar(255);
UPDATE person SET full_name = first_name + 'null' + last_name;
DECLARE @sql [nvarchar](MAX)
SELECT @sql = N'ALTER TABLE person DROP CONSTRAINT ' + QUOTENAME([df].[name]) FROM [sys].[columns] AS [c] INNER JOIN [sys].[default_constraints] AS [df] ON [df].[object_id] = [c].[default_object_id] WHERE [c].[object_id] = OBJECT_ID(N'person') AND [c].[name] = N'first_name'
EXEC sp_executesql @sql;
ALTER TABLE person DROP COLUMN first_name;
DECLARE @sql [nvarchar](MAX)
SELECT @sql = N'ALTER TABLE person DROP CONSTRAINT ' + QUOTENAME([df].[name]) FROM [sys].[columns] AS [c] INNER JOIN [sys].[default_constraints] AS [df] ON [df].[object_id] = [c].[default_object_id] WHERE [c].[object_id] = OBJECT_ID(N'person') AND [c].[name] = N'last_name'
EXEC sp_executesql @sql;
ALTER TABLE person DROP COLUMN last_name;
