# Test: liquibase.action.core.SnapshotTablesActionTest "case sensitive table in primary catalog and schema__unsupported" #

NOTE: This output is generated and parsed by TestMD. Please read it, but DO NOT EDIT MANUALLY

---------------------------------------

- **database:** liquibase.database.core.UnsupportedDatabase

| Permutation | Verified | catalogName | schemaName | tableName   | RESULTS
| :---------- | :------- | :---------- | :--------- | :---------- | :------
| bf94ef      | true     | lbcat       | lbschema   | OTHER_TABLE | **actions**: getTables(lbcat, lbschema, OTHER_TABLE, [TABLE])
| 48d5eb      | true     | lbcat       | lbschema   | TEST_TABLE  | **actions**: getTables(lbcat, lbschema, TEST_TABLE, [TABLE])


---------------------------------------

