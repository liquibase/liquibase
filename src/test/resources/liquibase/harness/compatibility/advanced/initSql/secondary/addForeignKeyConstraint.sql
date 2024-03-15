CREATE TABLE test_table_base (id INT NOT NULL, test_column INT NULL, CONSTRAINT PK_TEST_TABLE_BASE PRIMARY KEY (id));
CREATE TABLE test_table_reference (id INT NOT NULL, test_column INT NULL, CONSTRAINT PK_TEST_TABLE_REFERENCE PRIMARY KEY (id));
CREATE INDEX test_table_base_index ON test_table_base(test_column);
ALTER TABLE test_table_reference ADD CONSTRAINT secondary_test_fk FOREIGN KEY (id) REFERENCES test_table_base (test_column) ON UPDATE RESTRICT ON DELETE CASCADE;