-- Database: maxdb
-- Change Parameter: column1Name=first_name
-- Database: maxdb
-- Change Parameter: column2Name=last_name
-- Database: maxdb
-- Change Parameter: finalColumnName=full_name
-- Database: maxdb
-- Change Parameter: finalColumnType=varchar(255)
-- Database: maxdb
-- Change Parameter: tableName=person
ALTER TABLE person ADD full_name VARCHAR(255);
UPDATE person SET full_name = first_name || 'null' || last_name;
ALTER TABLE person DROP COLUMN first_name;
ALTER TABLE person DROP COLUMN last_name;
