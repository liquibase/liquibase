CREATE TABLE `harness_test_ds.test_table` (test_column INT);
ALTER TABLE `harness_test_ds.test_table` ADD COLUMN varcharColumn STRING(25), ADD COLUMN intColumn INT, ADD COLUMN dateColumn date;
ALTER TABLE `harness_test_ds.test_table` ALTER COLUMN varcharColumn SET DEFAULT 'INITIAL_VALUE';
ALTER TABLE `harness_test_ds.test_table` ALTER COLUMN intColumn SET DEFAULT 5;
ALTER TABLE `harness_test_ds.test_table` ALTER COLUMN dateColumn SET DEFAULT '2020-09-21';