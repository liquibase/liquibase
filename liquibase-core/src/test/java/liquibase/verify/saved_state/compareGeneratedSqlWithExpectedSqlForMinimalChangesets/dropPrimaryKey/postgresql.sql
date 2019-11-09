-- Database: postgresql
-- Change Parameter: tableName=person
DO $$ DECLARE constraint_name varchar;
BEGIN
  SELECT tc.CONSTRAINT_NAME into strict constraint_name
    FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS tc
    WHERE CONSTRAINT_TYPE = 'PRIMARY KEY'
      AND TABLE_NAME = 'person' AND TABLE_SCHEMA = 'null';
    EXECUTE 'alter table null.person drop constraint ' || constraint_name;
END $$;;
