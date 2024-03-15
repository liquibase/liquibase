CREATE TABLE test_table_xml (test_column INT NULL);
ALTER TABLE test_table_xml ADD CONSTRAINT secondary_check_constraint CHECK (test_column > 0);