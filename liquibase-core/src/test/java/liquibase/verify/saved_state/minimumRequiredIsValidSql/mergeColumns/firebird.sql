-- Database: firebird
-- Change Parameter: column1Name=first_name
-- Database: firebird
-- Change Parameter: column2Name=last_name
-- Database: firebird
-- Change Parameter: finalColumnName=full_name
-- Database: firebird
-- Change Parameter: finalColumnType=varchar(255)
-- Database: firebird
-- Change Parameter: tableName=person
ALTER TABLE person ADD full_name varchar(255);
UPDATE person SET full_name = first_name || 'null' || last_name;
ALTER TABLE person DROP first_name;
ALTER TABLE person DROP last_name;
