
Create Database statement:
CREATE DATABASE 'D:\asany\DBs\liquibase.db' LOG ON 'liquibase.log' COLLATION '1252LATIN1' NCHAR COLLATION 'UCA' DBA
USER 'dba' DBA PASSWORD 'liquibase';

CREATE DBSPACE "liquibase2" AS 'D:\\asany\\DBs\\liquibase2_dbspace.dbs';

Create a Windows service for the database (makes scripting easier):
dbsvc -as -t network -w liquibase d:\asany\bin64\dbsrv17.exe -x tcpip -c 8m d:\asany\DBs\liquibase.db

Alternatively (dbinit is a command line tool):
dbinit -z "1252LATIN1" -zn "UCA" -dba "dba","liquibase" -t "liquibase.log" "D:\asany\DBs\liquibase.db"

CREATE USER "liquibase" IDENTIFIED BY 'liquibase';

GRANT CREATE MATERIALIZED VIEW TO "liquibase";
GRANT CREATE PROCEDURE TO "liquibase";
GRANT CREATE TABLE TO "liquibase";

GRANT SELECT ANY TABLE TO "liquibase";
GRANT DELETE ANY TABLE TO "liquibase";
GRANT INSERT ANY TABLE TO "liquibase";
GRANT UPDATE ANY TABLE TO "liquibase";

GRANT CREATE ANY OBJECT TO "liquibase";
GRANT ALTER ANY OBJECT TO "liquibase";
GRANT DROP ANY OBJECT TO "liquibase";

GRANT CREATE VIEW TO "liquibase";
/* There seems to be no GRANT CREATE SEQUENCE in AS Anywhere */
GRANT CREATE ANY SEQUENCE TO "liquibase";

CREATE USER "lbcat2" IDENTIFIED BY 'liquibase';

GRANT CREATE MATERIALIZED VIEW TO "lbcat2";
GRANT CREATE PROCEDURE TO "lbcat2";
GRANT CREATE TABLE TO "lbcat2";
GRANT CREATE VIEW TO "lbcat2";
/* There seems to be no GRANT CREATE SEQUENCE in AS Anywhere */
GRANT CREATE ANY SEQUENCE TO "lbcat2";
