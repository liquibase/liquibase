-- Database: sybase
-- Change Parameter: newColumnName=full_name
-- Change Parameter: oldColumnName=name
-- Change Parameter: tableName=person
exec sp_rename 'person.name', 'full_name';
