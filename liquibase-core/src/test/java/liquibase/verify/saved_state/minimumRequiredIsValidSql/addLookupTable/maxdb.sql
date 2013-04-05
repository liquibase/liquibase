-- Database: maxdb
-- Change Parameter: existingColumnName=state
-- Database: maxdb
-- Change Parameter: existingTableName=address
-- Database: maxdb
-- Change Parameter: newColumnName=abbreviation
-- Database: maxdb
-- Change Parameter: newTableName=state
CREATE TABLE state AS SELECT DISTINCT state AS abbreviation FROM address WHERE state IS NOT NULL;
ALTER TABLE state COLUMN  abbreviation NOT NULL;
ALTER TABLE state ADD PRIMARY KEY (abbreviation);
ALTER TABLE address ADD CONSTRAINT FK_ADDRESS_STATE FOREIGN KEY (state) REFERENCES state (abbreviation);
