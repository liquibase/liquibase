
/*
 * WARNING: By default, new installations of Microsoft SQL Server only listen to named pipes. The default JDBC
 * connection string for the integration tests expect it to listen on the default TCP port 1433. Please use the
 * "SQL Server Configuration Manager" tool to enable TCP/IP on "SQL Server Network Configuration" ->
 * Protocols for MSSQLSERVER. You do not need to enable it for all network interfaces; localhost (127.0.0.1) is
 * sufficient for the tests.
 *
 * WARNING: You will probably want to adjust the path for the data files in the following script.
 */

CREATE DATABASE [liquibase]
 ON  PRIMARY 
( NAME = N'liquibase', FILENAME = N'D:\MSSQL\MSSQL13.MSSQLSERVER\MSSQL\DATA\liquibase.mdf' , SIZE = 8192KB , FILEGROWTH = 65536KB )
 LOG ON 
( NAME = N'liquibase_log', FILENAME = N'D:\MSSQL\MSSQL13.MSSQLSERVER\MSSQL\DATA\liquibase_log.ldf' , SIZE = 8192KB , FILEGROWTH = 65536KB )
GO

ALTER DATABASE [liquibase] SET COMPATIBILITY_LEVEL = 100
GO

USE [liquibase]
GO
IF NOT EXISTS (SELECT name FROM sys.filegroups WHERE is_default=1 AND name = N'PRIMARY') ALTER DATABASE [liquibase] MODIFY FILEGROUP [PRIMARY] DEFAULT
GO
ALTER DATABASE [liquibase] ADD FILEGROUP [liquibase2]
GO
ALTER DATABASE [liquibase] ADD FILE ( NAME = N'liquibase2', FILENAME = N'D:\MSSQL\MSSQL13.MSSQLSERVER\MSSQL\DATA\liquibase2.ndf' , SIZE = 8192KB , FILEGROWTH = 65536KB ) TO FILEGROUP [liquibase2]
GO

USE [master]
GO

CREATE LOGIN [lbuser] WITH PASSWORD=N'lbuser', DEFAULT_DATABASE=[liquibase], CHECK_EXPIRATION=OFF, CHECK_POLICY=OFF
GO
USE [liquibase]
GO
CREATE USER [lbuser] FOR LOGIN [lbuser]
GO
USE [liquibase]
GO
ALTER ROLE [db_owner] ADD MEMBER [lbuser]
GO

USE [liquibase]
GO
CREATE SCHEMA [lbcat2] AUTHORIZATION [dbo]
GO
