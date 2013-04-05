-- Database: mysql
-- Change Parameter: existingColumnName=state
-- Database: mysql
-- Change Parameter: existingTableName=address
-- Database: mysql
-- Change Parameter: newColumnDataType=char(2)
-- Database: mysql
-- Change Parameter: newColumnName=abbreviation
-- Database: mysql
-- Change Parameter: newTableName=state
CREATE TABLE state AS SELECT DISTINCT state AS abbreviation FROM address WHERE state IS NOT NULL;
ALTER TABLE state MODIFY abbreviation CHAR(2) NOT NULL;
ALTER TABLE state ADD PRIMARY KEY (abbreviation);
ALTER TABLE address ADD CONSTRAINT FK_ADDRESS_STATE FOREIGN KEY (state) REFERENCES state (abbreviation);
