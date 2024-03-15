CREATE TABLE test_table_base (id INT NOT NULL, test_column INT NULL, CONSTRAINT PK_TEST_TABLE_BASE PRIMARY KEY (id));
CREATE TABLE test_table_reference (id INT NOT NULL, test_column INT NULL, CONSTRAINT PK_TEST_TABLE_REFERENCE PRIMARY KEY (id));
CREATE INDEX test_table_reference_index ON test_table_reference(test_column);
ALTER TABLE test_table_base ADD CONSTRAINT test_fk FOREIGN KEY (id) REFERENCES test_table_reference (test_column) ON UPDATE RESTRICT ON DELETE CASCADE;