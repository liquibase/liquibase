# Test: liquibase.action.core.SnapshotTablesActionTest "case sensitive table in primary catalog and schema__h2" #

NOTE: This output is generated and parsed by TestMD. Please read it, but DO NOT EDIT MANUALLY

---------------------------------------

- **database:** liquibase.database.core.h2.H2Database

| Permutation | Verified | catalogName | schemaName | tableName   | RESULTS
| :---------- | :------- | :---------- | :--------- | :---------- | :------
| 3a22e7      | true     | LIQUIBASE   | PUBLIC     | OTHER_TABLE | **actions**: getTables(LIQUIBASE, PUBLIC, OTHER_TABLE, [TABLE])
| 9d06ff      | true     | LIQUIBASE   | PUBLIC     | TEST_TABLE  | **actions**: getTables(LIQUIBASE, PUBLIC, TEST_TABLE, [TABLE])


---------------------------------------

