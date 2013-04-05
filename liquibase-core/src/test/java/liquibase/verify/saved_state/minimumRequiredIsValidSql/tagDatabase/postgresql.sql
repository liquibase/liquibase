-- Database: postgresql
-- Change Parameter: tag=version_1.3
UPDATE databasechangelog SET TAG = 'version_1.3' WHERE DATEEXECUTED = (SELECT MAX(DATEEXECUTED) FROM databasechangelog);
