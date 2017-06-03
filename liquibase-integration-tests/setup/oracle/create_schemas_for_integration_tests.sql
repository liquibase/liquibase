
SET FEEDBACK OFF

DROP USER liquibase CASCADE;
DROP USER liquibaseb CASCADE;
DROP USER lbcat2 CASCADE;
DROP TABLESPACE liquibase INCLUDING CONTENTS AND DATAFILES;
DROP TABLESPACE liquibase2 INCLUDING CONTENTS AND DATAFILES;
DROP PROFILE lbtest;

/* Do not enforce password complexity or account expiration. For our test users */
CREATE PROFILE lbtest LIMIT
FAILED_LOGIN_ATTEMPTS 10
PASSWORD_LIFE_TIME UNLIMITED
PASSWORD_GRACE_TIME UNLIMITED
PASSWORD_LOCK_TIME UNLIMITED
PASSWORD_REUSE_TIME UNLIMITED
PASSWORD_REUSE_MAX UNLIMITED;

CREATE TABLESPACE liquibase;
CREATE TABLESPACE liquibase2;

CREATE USER liquibase IDENTIFIED BY "liquibase"
 DEFAULT TABLESPACE liquibase
 TEMPORARY TABLESPACE "TEMP"
 PROFILE lbtest
 QUOTA UNLIMITED ON liquibase;

ALTER USER liquibase QUOTA UNLIMITED ON liquibase2;

-- Privileges to do performance tuning (if necessary)
GRANT ADVISOR TO liquibase;
GRANT ADMINISTER SQL MANAGEMENT OBJECT TO liquibase;
GRANT ADMINISTER SQL TUNING SET TO liquibase;
GRANT CREATE ANY SQL PROFILE TO liquibase;
GRANT QUERY REWRITE TO liquibase;
GRANT ALTER SESSION TO liquibase;

-- Allow PL/SQL and Java debugging
GRANT DEBUG CONNECT SESSION TO liquibase;

-- Allow access to some useful V$ views
GRANT SELECT ON v_$statname TO liquibase;
GRANT SELECT ON v_$sesstat TO liquibase;
GRANT SELECT ON v_$session TO liquibase;
GRANT SELECT ON v_$sql TO liquibase;
GRANT SELECT ON v_$sql_plan TO liquibase;
GRANT SELECT ON v_$parameter to liquibase;
GRANT SELECT ON v_$sql_plan_statistics_all TO liquibase;

-- Allow accessing the list of objects in the database recycle bin
GRANT SELECT ON dba_recyclebin TO liquibase;

-- Allow creating and altering objects in other schemas
GRANT CREATE ANY TABLE to liquibase;
GRANT CREATE ANY INDEX to liquibase;
GRANT CREATE ANY VIEW to liquibase;
GRANT CREATE ANY MATERIALIZED VIEW to liquibase;

GRANT ALTER ANY TABLE to liquibase;
GRANT ALTER ANY INDEX to liquibase;
GRANT ALTER ANY MATERIALIZED VIEW to liquibase;

GRANT DROP ANY TABLE to liquibase;
GRANT DROP ANY VIEW to liquibase;
GRANT DROP ANY INDEX to liquibase;
GRANT DROP ANY MATERIALIZED VIEW to liquibase;

GRANT SELECT ANY TABLE to liquibase;
GRANT INSERT ANY TABLE to liquibase;
GRANT UPDATE ANY TABLE to liquibase;
GRANT DELETE ANY TABLE to liquibase;

-- Allow creation of database objects in the user's own schema
GRANT CREATE DATABASE LINK TO liquibase;
GRANT CREATE DIMENSION TO liquibase;
GRANT CREATE MATERIALIZED VIEW TO liquibase;
GRANT CREATE PROCEDURE TO liquibase;
GRANT CREATE ROLE TO liquibase;
GRANT CREATE SEQUENCE TO liquibase;
GRANT CREATE SESSION TO liquibase;
GRANT CREATE SYNONYM TO liquibase;
GRANT CREATE TABLE TO liquibase;
GRANT CREATE TRIGGER TO liquibase;
GRANT CREATE TYPE TO liquibase;
GRANT CREATE VIEW TO liquibase;

-- Unlimited storage allowance
CREATE USER liquibaseb IDENTIFIED BY "should_not_login"
 DEFAULT TABLESPACE liquibase
 TEMPORARY TABLESPACE "TEMP"
 PROFILE lbtest
 QUOTA UNLIMITED ON liquibase;
ALTER USER liquibaseb QUOTA UNLIMITED ON liquibase2;

CREATE USER lbcat2 IDENTIFIED BY "should_not_login"
 DEFAULT TABLESPACE liquibase
 TEMPORARY TABLESPACE "TEMP"
 PROFILE lbtest
 QUOTA UNLIMITED ON liquibase;
ALTER USER lbcat2 QUOTA UNLIMITED ON liquibase2;

QUIT
