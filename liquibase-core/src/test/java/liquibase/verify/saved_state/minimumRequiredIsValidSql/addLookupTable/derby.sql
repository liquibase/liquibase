-- Database: derby
-- Change Parameter: existingColumnName=state
-- Database: derby
-- Change Parameter: existingTableName=address
-- Database: derby
-- Change Parameter: newColumnDataType=char(2)
-- Database: derby
-- Change Parameter: newColumnName=abbreviation
-- Database: derby
-- Change Parameter: newTableName=state
CREATE TABLE state AS SELECT DISTINCT state AS abbreviation FROM address WHERE state IS NOT NULL;
ALTER TABLE state ALTER COLUMN  abbreviation NOT NULL;
ALTER TABLE state ADD PRIMARY KEY (abbreviation);
ALTER TABLE address ADD CONSTRAINT FK_ADDRESS_STATE FOREIGN KEY (state) REFERENCES state (abbreviation);
