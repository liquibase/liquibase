USE DATABASE LB_DBEXT_INT_DB;
USE ROLE LB_INT_ROLE;

-- Drop and recreate schema (this removes all objects in the schema)
DROP SCHEMA IF EXISTS BASE_SCHEMA CASCADE;
CREATE SCHEMA BASE_SCHEMA;

-- Ensure the role has proper permissions
GRANT ALL PRIVILEGES ON SCHEMA BASE_SCHEMA TO ROLE LB_INT_ROLE;