-- Database: informix
-- Change Parameter: columnNames=id, name
-- Change Parameter: tableName=person
ALTER TABLE person ADD CONSTRAINT UNIQUE (id, name);
