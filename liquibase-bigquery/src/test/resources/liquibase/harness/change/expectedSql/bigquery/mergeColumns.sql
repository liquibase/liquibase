CREATE TABLE harness_test_ds.full_name_table (first_name STRING(50), last_name STRING(50))
INSERT INTO harness_test_ds.full_name_table (first_name) VALUES ('John')
UPDATE harness_test_ds.full_name_table SET last_name = 'Doe' WHERE first_name='John'
INSERT INTO harness_test_ds.full_name_table (first_name) VALUES ('Jane')
UPDATE harness_test_ds.full_name_table SET last_name = 'Doe' WHERE first_name='Jane'
ALTER TABLE harness_test_ds.full_name_table ADD COLUMN full_name STRING(255)
UPDATE harness_test_ds.full_name_table SET full_name = first_name || ' ' || last_name WHERE 1 = 1
ALTER TABLE harness_test_ds.full_name_table DROP COLUMN first_name
ALTER TABLE harness_test_ds.full_name_table DROP COLUMN last_name