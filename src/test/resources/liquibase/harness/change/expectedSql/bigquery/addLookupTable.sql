CREATE TABLE test_authors (name STRING, email STRING)
CREATE TABLE authors_data AS SELECT DISTINCT email AS authors_email FROM test_authors WHERE email IS NOT NULL