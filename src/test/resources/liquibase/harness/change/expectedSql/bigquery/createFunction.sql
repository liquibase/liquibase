INVALID TEST
'Functions are supported in Liquibase enterprise
CREATE FUNCTION test_function(x INT64, y INT64)
(x INT64, y INT64)
RETURNS FLOAT64
AS (
  (x + 4) / y
);
'