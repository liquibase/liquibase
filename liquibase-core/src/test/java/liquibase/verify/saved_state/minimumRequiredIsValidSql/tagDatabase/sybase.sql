-- Database: sybase
-- Change Parameter: tag=version_1.3
UPDATE [dbo].[DATABASECHANGELOG] SET [TAG] = 'version_1.3' WHERE DATEEXECUTED = (SELECT MAX(DATEEXECUTED) FROM [dbo].[DATABASECHANGELOG]);
