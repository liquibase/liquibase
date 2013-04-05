-- Database: postgresql
-- Change Parameter: existingColumnName=state
-- Database: postgresql
-- Change Parameter: existingTableName=address
-- Database: postgresql
-- Change Parameter: newColumnName=abbreviation
-- Database: postgresql
-- Change Parameter: newTableName=state
CREATE TABLE state AS SELECT DISTINCT state AS abbreviation FROM address WHERE state IS NOT NULL;
ALTER TABLE state ALTER COLUMN  abbreviation SET NOT NULL;
ALTER TABLE state ADD PRIMARY KEY (abbreviation);
ALTER TABLE address ADD CONSTRAINT FK_ADDRESS_STATE FOREIGN KEY (state) REFERENCES state (abbreviation);
