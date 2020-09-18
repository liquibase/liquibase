DECLARE @dataPath varchar(256);
DECLARE @logPath varchar(256);
SET @dataPath=(SELECT CAST(serverproperty('InstanceDefaultDataPath') AS varchar(256)));
SET @logPath=(SELECT CAST(serverproperty('InstanceDefaultLogPath') AS varchar(256)));

CREATE LOGIN [lbuser] with password=N'LiquibasePass1', CHECK_EXPIRATION=OFF;
GO

CREATE DATABASE lbcat;
GO

EXEC lbcat..sp_addsrvrolemember @loginame = N'lbuser', @rolename = N'sysadmin'
GO

/* By default, we set the compatibility level to the oldest version we are officially supporting. Note that there
 * are differences in behaviour, e.g. with implicit conversions of date and time values. See
 * https://docs.microsoft.com/en-us/sql/t-sql/functions/cast-and-convert-transact-sql for details.
 */
ALTER DATABASE [lbcat] SET COMPATIBILITY_LEVEL = 100
GO

USE [lbcat]
GO
ALTER DATABASE [lbcat] MODIFY FILEGROUP [PRIMARY] DEFAULT
GO
ALTER DATABASE [lbcat] ADD FILEGROUP [liquibase2]
GO

DECLARE @dataPath varchar(256);
DECLARE @logPath varchar(256);
SET @dataPath=(SELECT CAST(serverproperty('InstanceDefaultDataPath') AS varchar(256)));
SET @logPath=(SELECT CAST(serverproperty('InstanceDefaultLogPath') AS varchar(256)));

DECLARE @createSql varchar(2000);
SET @createSql = (SELECT 'ALTER DATABASE [lbcat] ADD FILE ( NAME = N''liquibase2'', FILENAME = N''' + @dataPath + 'liquibase2.ndf'' , SIZE = 8192KB , FILEGROWTH = 65536KB ) TO FILEGROUP [liquibase2]');
EXECUTE(@createSql);
GO


CREATE SCHEMA [lbcat2] AUTHORIZATION [dbo]
GO
