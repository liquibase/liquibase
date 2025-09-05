-- Expected SQL output for dropDatabase test harness tests

-- Test 1: Basic database drop
DROP DATABASE LBTEST_DROP_BASIC

-- Test 2: Drop with IF EXISTS (non-existent database)
DROP DATABASE IF EXISTS LBTEST_NONEXISTENT

-- Test 3: Drop with CASCADE
DROP DATABASE LBTEST_DROP_CASCADE CASCADE

-- Test 4: Drop with RESTRICT (explicit)
DROP DATABASE LBTEST_DROP_RESTRICT RESTRICT

-- Test 5: Drop with IF EXISTS (existing database)
DROP DATABASE IF EXISTS LBTEST_DROP_IFEXISTS