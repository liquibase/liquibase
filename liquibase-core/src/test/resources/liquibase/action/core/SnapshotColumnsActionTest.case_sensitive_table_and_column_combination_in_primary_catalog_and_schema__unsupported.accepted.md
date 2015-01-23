# Test: liquibase.action.core.SnapshotColumnsActionTest "case sensitive table and column combination in primary catalog and schema__unsupported" #

NOTE: This output is generated and parsed by TestMD. Please read it, but DO NOT EDIT MANUALLY

---------------------------------------

- **database:** liquibase.database.core.UnsupportedDatabase

| Permutation | Verified | catalogName | columnName   | schemaName | tableName   | RESULTS
| :---------- | :------- | :---------- | :----------- | :--------- | :---------- | :------
| 77af2b      | true     | lbcat       | OTHER_COLUMN | lbschema   | OTHER_TABLE | **actions**: getColumns(lbcat, lbschema, OTHER_TABLE, OTHER_COLUMN)
| b23517      | true     | lbcat       | OTHER_COLUMN | lbschema   | TEST_TABLE  | **actions**: getColumns(lbcat, lbschema, TEST_TABLE, OTHER_COLUMN)
| a1298e      | true     | lbcat       | TEST_COLUMN  | lbschema   | OTHER_TABLE | **actions**: getColumns(lbcat, lbschema, OTHER_TABLE, TEST_COLUMN)
| 8f4e07      | true     | lbcat       | TEST_COLUMN  | lbschema   | TEST_TABLE  | **actions**: getColumns(lbcat, lbschema, TEST_TABLE, TEST_COLUMN)


---------------------------------------

