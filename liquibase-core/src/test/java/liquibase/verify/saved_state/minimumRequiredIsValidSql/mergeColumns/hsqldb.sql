-- Database: hsqldb
-- Change Parameter: column1Name=first_name
-- Database: hsqldb
-- Change Parameter: column2Name=last_name
-- Database: hsqldb
-- Change Parameter: finalColumnName=full_name
-- Database: hsqldb
-- Change Parameter: finalColumnType=varchar(255)
-- Database: hsqldb
-- Change Parameter: tableName=person
ALTER TABLE person ADD full_name varchar(255);
UPDATE person SET full_name = CONCAT(first_name, CONCAT('null', last_name));
ALTER TABLE person DROP COLUMN first_name;
ALTER TABLE person DROP COLUMN last_name;
