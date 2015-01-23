# Test: liquibase.action.core.SnapshotColumnsActionTest "case sensitive table and column combination in primary catalog and schema__h2" #

NOTE: This output is generated and parsed by TestMD. Please read it, but DO NOT EDIT MANUALLY

---------------------------------------

- **database:** liquibase.database.core.H2Database

| Permutation | Verified | catalogName | columnName   | schemaName | tableName   | RESULTS
| :---------- | :------- | :---------- | :----------- | :--------- | :---------- | :------
| 13f4f9      | true     | LIQUIBASE   | OTHER_COLUMN | PUBLIC     | OTHER_TABLE | **actions**: getColumns(LIQUIBASE, PUBLIC, OTHER_TABLE, OTHER_COLUMN)
| 68ffe8      | true     | LIQUIBASE   | OTHER_COLUMN | PUBLIC     | TEST_TABLE  | **actions**: getColumns(LIQUIBASE, PUBLIC, TEST_TABLE, OTHER_COLUMN)
| cc3f5a      | true     | LIQUIBASE   | TEST_COLUMN  | PUBLIC     | OTHER_TABLE | **actions**: getColumns(LIQUIBASE, PUBLIC, OTHER_TABLE, TEST_COLUMN)
| 76ab42      | true     | LIQUIBASE   | TEST_COLUMN  | PUBLIC     | TEST_TABLE  | **actions**: getColumns(LIQUIBASE, PUBLIC, TEST_TABLE, TEST_COLUMN)


---------------------------------------

