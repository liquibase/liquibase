-- Database: informix
-- Change Parameter: existingColumnName=state
-- Database: informix
-- Change Parameter: existingTableName=address
-- Database: informix
-- Change Parameter: newColumnDataType=char(2)
-- Database: informix
-- Change Parameter: newColumnName=abbreviation
-- Database: informix
-- Change Parameter: newTableName=state
CREATE TABLE state ( abbreviation char(2) );
INSERT INTO state ( abbreviation ) SELECT DISTINCT state FROM address WHERE state IS NOT NULL;
ALTER TABLE state MODIFY (abbreviation CHAR(2) NOT NULL);
ALTER TABLE state ADD CONSTRAINT PRIMARY KEY (abbreviation);
ALTER TABLE address ADD CONSTRAINT  FOREIGN KEY (state) REFERENCES state (abbreviation) CONSTRAINT FK_ADDRESS_STATE;
