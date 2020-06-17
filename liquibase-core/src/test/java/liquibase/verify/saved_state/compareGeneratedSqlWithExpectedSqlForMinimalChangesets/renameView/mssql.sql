-- Database: mssql
-- Change Parameter: newViewName=v_person
-- Change Parameter: oldViewName=v_person
exec sp_rename 'v_person', 'v_person';
