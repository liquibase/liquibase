
CREATE DATABASE IF NOT EXISTS liquibase WITH LOG;
-- (Login as superuser (informix) into the new DB)
GRANT CONNECT TO liquibase;
GRANT RESOURCE to liquibase;
/*
 * The DBA privilege is needed to perform the integration tests that write into an alternative schema. Unfortunately,
 * there seems to be no smaller privilege in Informix to make this possible.
 */
GRANT DBA to liquibase;

CREATE DATABASE IF NOT EXISTS liquibasec WITH LOG;
-- (Login as superuser (informix) into the new DB)
GRANT CONNECT TO liquibase;
GRANT RESOURCE to liquibase;
GRANT DBA to liquibase;

-- (login as superuser (informix) into the dbadmin database!)

EXECUTE FUNCTION task ("create dbspace", "liquibase2", "d:\informix\storage\liquibase2", "20 M", "0");