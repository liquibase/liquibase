-- Database: mysql
-- Change Parameter: tag=version_1.3
UPDATE DATABASECHANGELOG SET TAG = 'version_1.3' WHERE DATEEXECUTED = (SELECT MAX(DATEEXECUTED) FROM (SELECT DATEEXECUTED FROM DATABASECHANGELOG) AS X);
