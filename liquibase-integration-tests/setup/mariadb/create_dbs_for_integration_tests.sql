-- --------------------------------------------------------
-- Host:                         127.0.0.1
-- Server Version:               10.1.23-MariaDB - mariadb.org binary distribution
-- Server Betriebssystem:        Win64
-- HeidiSQL Version:             9.4.0.5125
-- --------------------------------------------------------

/* INFO: On Windows, the MariaDB service is installed with a Display Name of "MySQL" which can be confusing
   if you want to run integration tests for both MariaDB and MySQL. I would recommend to change at least the service
   display name from an administrative command line after installation:

   create_dbs_for_integration_tests.sql
 */

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

DROP DATABASE IF EXISTS `lbcat`;
CREATE DATABASE IF NOT EXISTS `lbcat` /*!40100 DEFAULT CHARACTER SET utf8 */;
USE `lbcat`;

DROP DATABASE IF EXISTS `liquibase`;
CREATE DATABASE IF NOT EXISTS `liquibase` /*!40100 DEFAULT CHARACTER SET utf8 */;
USE `liquibase`;

DROP DATABASE IF EXISTS `lbcat2`;
CREATE DATABASE IF NOT EXISTS `lbcat2` /*!40100 DEFAULT CHARACTER SET utf8 */;
USE `lbcat2`;

DROP USER IF EXISTS 'lbuser'@'localhost';

CREATE USER 'lbuser'@'localhost' IDENTIFIED BY 'lbuser';
GRANT EXECUTE, PROCESS, SELECT, SHOW DATABASES, SHOW VIEW, ALTER, ALTER ROUTINE, CREATE, CREATE ROUTINE, CREATE TABLESPACE, CREATE TEMPORARY TABLES, CREATE VIEW, DELETE, DROP, EVENT, INDEX, INSERT, REFERENCES, TRIGGER, UPDATE, CREATE USER, FILE, LOCK TABLES, RELOAD, REPLICATION CLIENT, REPLICATION SLAVE, SHUTDOWN, SUPER  ON *.* TO 'lbuser'@'localhost' WITH GRANT OPTION;
FLUSH PRIVILEGES;
