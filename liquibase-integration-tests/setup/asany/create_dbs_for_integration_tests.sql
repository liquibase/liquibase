
Create Database statement:
CREATE DATABASE 'D:\asany\DBs\liquibase.db' LOG ON 'liquibase.log' COLLATION '1252LATIN1' NCHAR COLLATION 'UCA' DBA
USER 'dba' DBA PASSWORD 'liquibase';

Alternatively (dbinit is a command line tool):
dbinit -z "1252LATIN1" -zn "UCA" -dba "dba","liquibase" -t "liquibase.log" "D:\asany\DBs\liquibase.db"

CREATE USER "liquibase" IDENTIFIED BY 'liquibase';

GRANT CREATE MATERIALIZED VIEW TO "liquibase";
GRANT CREATE PROCEDURE TO "liquibase";
GRANT CREATE TABLE TO "liquibase";
GRANT CREATE VIEW TO "liquibase";

