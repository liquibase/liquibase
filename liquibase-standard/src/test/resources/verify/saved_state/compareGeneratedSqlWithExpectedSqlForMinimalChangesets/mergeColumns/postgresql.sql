-- Database: postgresql
-- Change Parameter: column1Name=first_name
-- Change Parameter: column2Name=last_name
-- Change Parameter: finalColumnName=full_name
-- Change Parameter: finalColumnType=varchar(255)
-- Change Parameter: tableName=person
ALTER TABLE person ADD full_name VARCHAR(255);
UPDATE person SET full_name = first_name || 'null' || last_name;
ALTER TABLE person DROP COLUMN first_name;
ALTER TABLE person DROP COLUMN last_name;
