SET ECHO ON;
WHENEVER SQLERROR EXIT SQL.SQLCODE;

-- Set the session container to lbcat
PROMPT "configuring lbcat"
ALTER SESSION SET container=lbcat; 

-- Create lbcat USERS tablespace
CREATE TABLESPACE USERS 
   DATAFILE '/opt/oracle/oradata/ORCLCDB/lbcat/users01.dbf' SIZE 256K 
   AUTOEXTEND ON NEXT 256K; 
ALTER PLUGGABLE DATABASE DEFAULT TABLESPACE USERS;  

-- Create roles and "lbuser" user
@/opt/oracle/scripts/startup/support/roles.sql lbcat

-- -----------------------------------------------------------------------------

-- Set the session container to lbcat2
PROMPT "configuring lbcat2"
ALTER SESSION SET container=lbcat2; 

-- Create lbcat2 USERS tablespace
CREATE TABLESPACE USERS 
   DATAFILE '/opt/oracle/oradata/ORCLCDB/lbcat2/users01.dbf' SIZE 256K 
   AUTOEXTEND ON NEXT 256K; 
ALTER PLUGGABLE DATABASE DEFAULT TABLESPACE USERS;  

-- Create roles and "lbuser" user
@/opt/oracle/scripts/startup/support/roles.sql lbcat2

-- 
-- Reset the session container 
-- 
ALTER SESSION SET CONTAINER=CDB$ROOT; 