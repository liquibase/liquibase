-- Database: mssql
-- Change Parameter: existingColumnName=state
-- Database: mssql
-- Change Parameter: existingTableName=address
-- Database: mssql
-- Change Parameter: newColumnDataType=char(2)
-- Database: mssql
-- Change Parameter: newColumnName=abbreviation
-- Database: mssql
-- Change Parameter: newTableName=state
SELECT DISTINCT state AS abbreviation INTO [state] FROM [dbo].[address] WHERE state IS NOT NULL;
ALTER TABLE [state] ALTER COLUMN [abbreviation] CHAR(2) NOT NULL;
ALTER TABLE [state] ADD PRIMARY KEY ([abbreviation]);
ALTER TABLE [dbo].[address] ADD CONSTRAINT [FK_ADDRESS_STATE] FOREIGN KEY ([state]) REFERENCES [state] ([abbreviation]);
