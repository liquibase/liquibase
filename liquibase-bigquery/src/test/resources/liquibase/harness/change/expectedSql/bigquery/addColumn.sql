ALTER TABLE harness_test_ds.authors ADD COLUMN varcharColumn STRING
ALTER TABLE harness_test_ds.authors ADD COLUMN intColumn INT64
ALTER TABLE harness_test_ds.authors ADD COLUMN dateColumn date
UPDATE harness_test_ds.authors SET varcharColumn = 'INITIAL_VALUE' WHERE 1 = 1
UPDATE harness_test_ds.authors SET intColumn = 5 WHERE 1 = 1
UPDATE harness_test_ds.authors SET dateColumn = '2020-09-21' WHERE 1 = 1