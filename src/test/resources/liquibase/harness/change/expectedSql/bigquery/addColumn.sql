INVALID TEST
---
'BigQuery does not handle properly column drops which is tested as last step in this test
ALTER TABLE lharness.authors ADD COLUMN varcharColumn STRING
ALTER TABLE lharness.authors ADD COLUMN intColumn INT64
ALTER TABLE lharness.authors ADD COLUMN dateColumn date
UPDATE lharness.authors SET varcharColumn = "INITIAL_VALUE" WHERE 1=1
UPDATE lharness.authors SET intColumn = 5 WHERE 1=1
UPDATE lharness.authors SET dateColumn = "2020-09-21" WHERE 1=1
'
