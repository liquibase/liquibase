-- Database: sybase
-- Change Parameter: newTableName=employee
-- Change Parameter: oldTableName=person
exec sp_rename 'person', 'employee';
