
/*
 * IMPORTANT!
 *
 * For this script to work, you MUST adjust the tablespace directory at the end of this script so that it fits
 * your environment (unless you are running pgsql 9.6 on Windows using the default installation directory)
 */

CREATE USER lbuser WITH
	LOGIN
	NOSUPERUSER
	NOCREATEDB
	NOCREATEROLE
	INHERIT
	NOREPLICATION
	CONNECTION LIMIT -1
	PASSWORD 'lbuser';

COMMENT ON ROLE lbuser IS 'Integration test user for DB-Manul (based on Liquibase)';

CREATE DATABASE liquibase
WITH
OWNER = postgres
ENCODING = 'UTF8'
CONNECTION LIMIT = -1;

COMMENT ON DATABASE liquibase
IS 'LB catalog/database for integration tests';

GRANT ALL ON DATABASE liquibase TO lbuser;

CREATE SCHEMA lbschem2
	AUTHORIZATION postgres;

COMMENT ON SCHEMA lbschem2
IS 'Testing schema for integration tests';

GRANT ALL ON SCHEMA lbschem2 TO lbuser;

ALTER DEFAULT PRIVILEGES IN SCHEMA lbschem2
GRANT ALL ON TABLES TO lbuser;

ALTER DEFAULT PRIVILEGES IN SCHEMA lbschem2
GRANT SELECT, USAGE ON SEQUENCES TO lbuser;

ALTER DEFAULT PRIVILEGES IN SCHEMA lbschem2
GRANT EXECUTE ON FUNCTIONS TO lbuser;

ALTER DEFAULT PRIVILEGES IN SCHEMA lbschem2
GRANT USAGE ON TYPES TO lbuser;

CREATE TABLESPACE liquibase2
	OWNER postgres
LOCATION 'C:\Program Files\PostgreSQL\9.6\data';

ALTER TABLESPACE liquibase2
OWNER TO postgres;

COMMENT ON TABLESPACE liquibase2
IS 'A testing tablespace for integration tests';

GRANT CREATE ON TABLESPACE liquibase2 TO lbuser;

CREATE SCHEMA lbcat2
	AUTHORIZATION postgres;

COMMENT ON SCHEMA lbcat2
IS 'Testing schema for integration tests';

GRANT ALL ON SCHEMA lbcat2 TO lbuser;

ALTER DEFAULT PRIVILEGES IN SCHEMA lbcat2
GRANT ALL ON TABLES TO lbuser;

ALTER DEFAULT PRIVILEGES IN SCHEMA lbcat2
GRANT SELECT, USAGE ON SEQUENCES TO lbuser;

ALTER DEFAULT PRIVILEGES IN SCHEMA lbcat2
GRANT EXECUTE ON FUNCTIONS TO lbuser;

ALTER DEFAULT PRIVILEGES IN SCHEMA lbcat2
GRANT USAGE ON TYPES TO lbuser;
