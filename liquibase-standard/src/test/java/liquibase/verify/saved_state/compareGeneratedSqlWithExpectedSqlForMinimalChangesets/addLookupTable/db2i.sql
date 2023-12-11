-- Database: db2i
-- Change Parameter: existingColumnName=state
-- Change Parameter: existingTableName=address
-- Change Parameter: newColumnName=abbreviation
-- Change Parameter: newTableName=state
CREATE TABLE state AS (SELECT state AS abbreviation FROM address) WITH NO DATA;
INSERT INTO state SELECT DISTINCT state FROM address WHERE state IS NOT NULL;
ALTER TABLE state ALTER COLUMN  abbreviation SET NOT NULL;
ALTER TABLE state ADD PRIMARY KEY (abbreviation);
ALTER TABLE address ADD CONSTRAINT FK_ADDRESS_STATE FOREIGN KEY (state) REFERENCES state (abbreviation);
