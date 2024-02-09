-- Database: informix
-- Change Parameter: newColumnName=full_name
-- Change Parameter: oldColumnName=name
-- Change Parameter: tableName=person
RENAME COLUMN person.name TO full_name;
