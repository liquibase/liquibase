-- Database: cache
-- Change Parameter: existingColumnName=state
-- Database: cache
-- Change Parameter: existingTableName=address
-- Database: cache
-- Change Parameter: newColumnName=abbreviation
-- Database: cache
-- Change Parameter: newTableName=state
CREATE TABLE state AS SELECT DISTINCT state AS abbreviation FROM address WHERE state IS NOT NULL;
ALTER TABLE state ALTER COLUMN  abbreviation NOT NULL;
ALTER TABLE state ADD PRIMARY KEY (abbreviation);
ALTER TABLE address ADD CONSTRAINT FK_ADDRESS_STATE FOREIGN KEY (state) REFERENCES state (abbreviation);
