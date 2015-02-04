# Test: liquibase.action.core.SnapshotColumnsActionTest "case sensitive table and column combination in primary catalog and schema__h2" #

NOTE: This output is generated and parsed by TestMD. Please read it, but DO NOT EDIT MANUALLY

---------------------------------------

- **database:** liquibase.database.core.h2.H2Database

| Permutation | Verified | catalogName | columnName   | schemaName | tableName   | RESULTS
| :---------- | :------- | :---------- | :----------- | :--------- | :---------- | :------
| 9ac162      | true     | LIQUIBASE   | OTHER_COLUMN | PUBLIC     | OTHER_TABLE | **actions**: getColumns(LIQUIBASE, PUBLIC, OTHER_TABLE, OTHER_COLUMN)
| 8acd67      | true     | LIQUIBASE   | OTHER_COLUMN | PUBLIC     | TEST_TABLE  | **actions**: getColumns(LIQUIBASE, PUBLIC, TEST_TABLE, OTHER_COLUMN)
| 38fc37      | true     | LIQUIBASE   | TEST_COLUMN  | PUBLIC     | OTHER_TABLE | **actions**: getColumns(LIQUIBASE, PUBLIC, OTHER_TABLE, TEST_COLUMN)
| a79edd      | true     | LIQUIBASE   | TEST_COLUMN  | PUBLIC     | TEST_TABLE  | **actions**: getColumns(LIQUIBASE, PUBLIC, TEST_TABLE, TEST_COLUMN)


---------------------------------------

