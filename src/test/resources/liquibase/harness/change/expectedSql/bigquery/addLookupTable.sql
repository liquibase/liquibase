CREATE TABLE authors_data AS SELECT DISTINCT email AS authors_email FROM authors WHERE email IS NOT NULL
