
CREATE DATABASE IF NOT EXISTS liquibase WITH LOG;

-- (Login as superuser (informix) into the new DB)

GRANT CONNECT TO liquibase;
GRANT RESOURCE to liquibase;


