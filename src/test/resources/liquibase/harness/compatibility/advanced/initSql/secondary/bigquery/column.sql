ALTER TABLE `harness_test_ds.test_table` ADD COLUMN varcharColumn STRING(50);
ALTER TABLE `harness_test_ds.test_table` ALTER COLUMN varcharColumn SET DEFAULT 'DIFFERENT_VALUE';