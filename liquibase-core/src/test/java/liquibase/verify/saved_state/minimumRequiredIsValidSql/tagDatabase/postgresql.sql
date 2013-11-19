-- Database: postgresql
-- Change Parameter: tag=version_1.3
UPDATE databasechangelog SET TAG = 'version_1.3' WHERE ORDEREXECUTED > (SELECT oe FROM (   SELECT   	CASE   		WHEN max(ORDEREXECUTED) IS NULL THEN 0   		ELSE max(ORDEREXECUTED)   	END as oe   FROM databasechangelog WHERE TAG IS NOT NULL) AS X);
