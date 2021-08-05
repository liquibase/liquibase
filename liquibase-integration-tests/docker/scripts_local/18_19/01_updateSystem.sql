SET ECHO ON;
WHENEVER SQLERROR EXIT SQL.SQLCODE;

ALTER USER SYSTEM IDENTIFIED BY "R2B09T6iC4zxBTZ8";

-- set undo tablespace to unlimited
ALTER DATABASE datafile '/opt/oracle/oradata/ORCLCDB/undotbs01.dbf' autoextend ON maxsize UNLIMITED;

ALTER SYSTEM SET recyclebin = OFF DEFERRED;
ALTER SYSTEM SET "_cdb_disable_pdb_limit" =true scope=SPFILE;
 
SHUTDOWN IMMEDIATE;
STARTUP;