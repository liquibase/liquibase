-- Expected SQL output for createDatabase test harness tests

-- Test 1: Basic database creation
CREATE DATABASE LBTEST_BASIC_DB

-- Test 2: Transient database (no retention)
CREATE TRANSIENT DATABASE LBTEST_TRANSIENT_DB

-- Test 3: Database with retention settings and comment
CREATE DATABASE LBTEST_RETENTION_DB DATA_RETENTION_TIME_IN_DAYS = 7 MAX_DATA_EXTENSION_TIME_IN_DAYS = 30 COMMENT = 'Test database with retention settings'

-- Test 4a: Initial database
CREATE DATABASE LBTEST_REPLACE_DB COMMENT = 'Initial database'

-- Test 4b: OR REPLACE database
CREATE OR REPLACE DATABASE LBTEST_REPLACE_DB COMMENT = 'Replaced database'

-- Test 5a: Initial conditional database
CREATE DATABASE LBTEST_CONDITIONAL_DB COMMENT = 'Initial conditional database'

-- Test 5b: IF NOT EXISTS database
CREATE DATABASE IF NOT EXISTS LBTEST_CONDITIONAL_DB COMMENT = 'Should not replace existing'

-- Test 6a: Source database for cloning
CREATE DATABASE LBTEST_CLONE_SOURCE_DB COMMENT = 'Source database for cloning'

-- Test 6b: Clone database
CREATE DATABASE LBTEST_CLONED_DB CLONE LBTEST_CLONE_SOURCE_DB COMMENT = 'Cloned database'

-- Test 7: Database with collation
CREATE DATABASE LBTEST_COLLATION_DB DEFAULT_DDL_COLLATION = 'en-ci' COMMENT = 'Database with custom collation'