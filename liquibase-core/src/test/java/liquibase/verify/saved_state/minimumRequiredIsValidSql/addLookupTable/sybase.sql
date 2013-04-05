-- Database: sybase
-- Change Parameter: existingColumnName=state
-- Database: sybase
-- Change Parameter: existingTableName=address
-- Database: sybase
-- Change Parameter: newColumnName=abbreviation
-- Database: sybase
-- Change Parameter: newTableName=state
CREATE TABLE [state] AS SELECT DISTINCT state AS abbreviation FROM [dbo].[address] WHERE state IS NOT NULL;
ALTER TABLE [state] MODIFY [abbreviation] NOT NULL;
ALTER TABLE [state] ADD PRIMARY KEY ([abbreviation]);
ALTER TABLE [dbo].[address] ADD CONSTRAINT [FK_ADDRESS_STATE] FOREIGN KEY ([state]) REFERENCES [state] ([abbreviation]);
