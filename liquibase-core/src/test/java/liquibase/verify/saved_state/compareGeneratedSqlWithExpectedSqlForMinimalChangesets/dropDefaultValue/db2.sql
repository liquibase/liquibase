-- Database: db2
-- Change Parameter: columnName=id
-- Change Parameter: tableName=person
BEGIN
  DECLARE v_default CLOB;

  SELECT C.default INTO v_default FROM syscat.columns C
  WHERE tabname='PERSON'
        AND colname='ID'
        AND tabschema='null';

  IF v_default IS NOT NULL THEN
    IF v_default <> 'NULL' THEN
      EXECUTE IMMEDIATE 'ALTER TABLE person ALTER COLUMN id DROP DEFAULT';
    END IF;
  END IF;
END;
