SET ECHO ON;
WHENEVER SQLERROR EXIT SQL.SQLCODE;


DROP PUBLIC SYNONYM LOCAL_CHUNK_TYPES;
DROP PUBLIC SYNONYM LOCAL_CHUNKS;
DROP PUBLIC SYNONYM SHA_DATABASES;
DROP PUBLIC SYNONYM LOCAL_CHUNK_COLUMNS; 

-- set processes to higher limit to avoid ORA-39014, ORA-39029, ORA-31671, ORA-00600
ALTER SYSTEM SET open_cursors=1024 scope=both sid='*';
ALTER SYSTEM SET "_optimizer_cost_based_transformation"=off scope=both sid='*';
--ALTER SYSTEM SET processes=1000 scope=spfile;
--ALTER SYSTEM SET sessions=665 scope=spfile;
--ALTER SYSTEM SET transactions=700 scope=spfile;

-- Restart database after setting higher process limit
SHUTDOWN IMMEDIATE;
STARTUP;