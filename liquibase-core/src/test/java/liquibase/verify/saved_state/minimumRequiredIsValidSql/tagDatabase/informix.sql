-- Database: informix
-- Change Parameter: tag=version_1.3
SELECT MAX(dateexecuted) max_date FROM DATABASECHANGELOG INTO TEMP max_date_temp WITH NO LOG;
UPDATE DATABASECHANGELOG SET TAG = 'version_1.3' WHERE DATEEXECUTED = (SELECT max_date FROM max_date_temp);;
DROP TABLE max_date_temp;;
