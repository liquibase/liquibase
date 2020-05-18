#!/usr/bin/env bash
set -e

mkdir -p /tmp/tablespace/liquibase2

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE USER lbuser WITH PASSWORD 'LiquibasePass1';
    CREATE DATABASE lbcat2;
    CREATE SCHEMA lbcat2;
    CREATE TABLESPACE liquibase2 OWNER lbuser LOCATION '/tmp/tablespace/liquibase2';
    GRANT ALL PRIVILEGES ON DATABASE lbcat TO lbuser;
    GRANT ALL PRIVILEGES ON DATABASE lbcat2 TO lbuser;
    GRANT ALL PRIVILEGES ON SCHEMA lbcat2 TO lbuser;
EOSQL