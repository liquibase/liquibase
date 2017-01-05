-- Database: sybase
-- Change Parameter: existingColumnName=state
-- Change Parameter: existingTableName=address
-- Change Parameter: newColumnName=abbreviation
-- Change Parameter: newTableName=state
CREATE TABLE [state] AS SELECT DISTINCT [state] AS [abbreviation] FROM [address] WHERE [state] IS NOT NULL;
ALTER TABLE [state] MODIFY [abbreviation] NOT NULL;
ALTER TABLE [state] ADD PRIMARY KEY ([abbreviation]);
ALTER TABLE [address] ADD CONSTRAINT [FK_ADDRESS_STATE] FOREIGN KEY ([state]) REFERENCES [state] ([abbreviation]);
