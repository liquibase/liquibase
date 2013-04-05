-- Database: informix
-- Change Parameter: column1Name=first_name
-- Database: informix
-- Change Parameter: column2Name=last_name
-- Database: informix
-- Change Parameter: finalColumnName=full_name
-- Database: informix
-- Change Parameter: finalColumnType=varchar(255)
-- Database: informix
-- Change Parameter: tableName=person
ALTER TABLE person ADD full_name varchar(255);
UPDATE person SET full_name = first_name || 'null' || last_name;
ALTER TABLE person DROP first_name;
ALTER TABLE person DROP last_name;
