
/*
 * IMPORTANT!
 *
 * If you want to run this script outside of the CI environment, you must replace :path_for_tablespace with an
 * absolute path on your filesystem. This path must already exist and it should be empty.
 */

DROP DATABASE IF EXISTS liquibase;
DROP TABLESPACE IF EXISTS liquibase2;
DROP SCHEMA IF EXISTS lbschem2 CASCADE;
DROP SCHEMA IF EXISTS lbcat2 CASCADE;
DROP USER IF EXISTS lbuser;

CREATE USER lbuser WITH
  LOGIN
  NOSUPERUSER
  NOCREATEDB
  NOCREATEROLE
INHERIT
  NOREPLICATION
  CONNECTION LIMIT -1
PASSWORD 'lbuser';

COMMENT ON ROLE lbuser IS 'Integration test user for Liquibase';

CREATE DATABASE liquibase
WITH
    OWNER = default
          ENCODING = 'UTF8'
          CONNECTION LIMIT = -1;

COMMENT ON DATABASE liquibase IS 'LB catalog/database for integration tests';

GRANT ALL ON DATABASE liquibase TO lbuser;

\c liquibase

/************************************************************************************************* Schema: LBSCHEM2 */
CREATE SCHEMA lbschem2 AUTHORIZATION lbuser;

COMMENT ON SCHEMA lbschem2 IS 'Testing schema for integration tests';

GRANT ALL ON SCHEMA lbschem2 TO lbuser;

ALTER DEFAULT PRIVILEGES IN SCHEMA lbschem2
GRANT ALL ON TABLES TO lbuser;

ALTER DEFAULT PRIVILEGES IN SCHEMA lbschem2
GRANT SELECT, USAGE ON SEQUENCES TO lbuser;

ALTER DEFAULT PRIVILEGES IN SCHEMA lbschem2
GRANT EXECUTE ON FUNCTIONS TO lbuser;

ALTER DEFAULT PRIVILEGES IN SCHEMA lbschem2
GRANT USAGE ON TYPES TO lbuser;

/******************************************************************************************** Tablespace: liquibase2 */

CREATE TABLESPACE liquibase2 LOCATION :path_for_tablespace;

ALTER TABLESPACE liquibase2 OWNER TO lbuser;

COMMENT ON TABLESPACE liquibase2 IS 'A testing tablespace for integration tests';

GRANT CREATE ON TABLESPACE liquibase2 TO lbuser;

/**************************************************************************************************** Schema: LBCAT2 */

CREATE SCHEMA lbcat2 AUTHORIZATION lbuser;

COMMENT ON SCHEMA lbcat2 IS 'Testing schema for integration tests';

GRANT ALL ON SCHEMA lbcat2 TO lbuser;

ALTER DEFAULT PRIVILEGES IN SCHEMA lbcat2
GRANT ALL ON TABLES TO lbuser;

ALTER DEFAULT PRIVILEGES IN SCHEMA lbcat2
GRANT SELECT, USAGE ON SEQUENCES TO lbuser;

ALTER DEFAULT PRIVILEGES IN SCHEMA lbcat2
GRANT EXECUTE ON FUNCTIONS TO lbuser;

ALTER DEFAULT PRIVILEGES IN SCHEMA lbcat2
GRANT USAGE ON TYPES TO lbuser;
