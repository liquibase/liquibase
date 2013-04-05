-- Database: hsqldb
-- Change Parameter: existingColumnName=state
-- Database: hsqldb
-- Change Parameter: existingTableName=address
-- Database: hsqldb
-- Change Parameter: newColumnName=abbreviation
-- Database: hsqldb
-- Change Parameter: newTableName=state
CREATE TABLE state AS SELECT DISTINCT state AS abbreviation FROM address WHERE state IS NOT NULL;
ALTER TABLE state ALTER COLUMN abbreviation SET NOT NULL;
ALTER TABLE state ADD PRIMARY KEY (abbreviation);
ALTER TABLE address ADD CONSTRAINT FK_ADDRESS_STATE FOREIGN KEY (state) REFERENCES state (abbreviation);
