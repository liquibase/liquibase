-- Database: unsupported
-- Change Parameter: existingColumnName=state
-- Database: unsupported
-- Change Parameter: existingTableName=address
-- Database: unsupported
-- Change Parameter: newColumnName=abbreviation
-- Database: unsupported
-- Change Parameter: newTableName=state
CREATE TABLE state AS SELECT DISTINCT state AS abbreviation FROM address WHERE state IS NOT NULL;
ALTER TABLE state ALTER COLUMN  abbreviation SET NOT NULL;
ALTER TABLE state ADD PRIMARY KEY (abbreviation);
ALTER TABLE address ADD CONSTRAINT FK_ADDRESS_STATE FOREIGN KEY (state) REFERENCES state (abbreviation);
