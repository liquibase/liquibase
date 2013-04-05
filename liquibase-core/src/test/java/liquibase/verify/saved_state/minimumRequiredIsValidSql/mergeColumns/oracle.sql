-- Database: oracle
-- Change Parameter: column1Name=first_name
-- Database: oracle
-- Change Parameter: column2Name=last_name
-- Database: oracle
-- Change Parameter: finalColumnName=full_name
-- Database: oracle
-- Change Parameter: finalColumnType=varchar(255)
-- Database: oracle
-- Change Parameter: tableName=person
ALTER TABLE person ADD full_name varchar(255);
UPDATE person SET full_name = first_name || 'null' || last_name;
ALTER TABLE person DROP COLUMN first_name;
ALTER TABLE person DROP COLUMN last_name;
