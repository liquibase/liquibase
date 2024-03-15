CREATE TABLE secondarydb.test_table (id INT NULL, test_column INT NULL);
CREATE INDEX idx_secondary ON secondarydb.test_table(test_column);