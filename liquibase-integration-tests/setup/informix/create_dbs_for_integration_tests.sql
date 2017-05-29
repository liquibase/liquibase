
CREATE DATABASE IF NOT EXISTS liquibase WITH LOG;

-- (Login as superuser (informix) into the new DB)

GRANT CONNECT TO liquibase;
GRANT RESOURCE to liquibase;

-- (login as superuser (informix) into the dbadmin database!)

EXECUTE FUNCTION task ("create dbspace", "liquibase2",
"d:\informix\storage\liquibase2", "20 M", "0");