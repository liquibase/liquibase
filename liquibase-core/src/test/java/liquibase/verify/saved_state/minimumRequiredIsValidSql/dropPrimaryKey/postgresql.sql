-- Database: postgresql
-- Change Parameter: tableName=person
create or replace function __liquibase_drop_pk(tableName text) returns void as $$ declare pkname text; sql text; begin pkname = c.conname from pg_class r, pg_constraint c where r.oid = c.conrelid and contype = 'p' and relname ilike tableName; sql = 'alter table ' || tableName || ' drop constraint ' || pkname; execute sql; end; $$ language plpgsql; select __liquibase_drop_pk('person'); drop function __liquibase_drop_pk(tableName text);;
