**NOTE: This output is generated and parsed by TestMD. Please read it, but DO NOT EDIT MANUALLY**

# Test: "can snapshot all tables in schema" #

- **connection:** mysql[config:caseInsensitive]

| Permutation | Verified | schemaName      | OPERATIONS
| :---------- | :------- | :-------------- | :------
| 3cd221      | true     | lbcat (SCHEMA)  | **plan**: getTables(lbcat, null, null, [TABLE])
| 3193e6      | true     | lbcat2 (SCHEMA) | **plan**: getTables(lbcat2, null, null, [TABLE])

# Test: "can snapshot all tables in schema using a null table name reference" #

- **connection:** mysql[config:caseInsensitive]

| Permutation | Verified | schemaName      | OPERATIONS
| :---------- | :------- | :-------------- | :------
| 3cd221      | true     | lbcat (SCHEMA)  | **plan**: getTables(null, null, lbcat, [TABLE])
| 3193e6      | true     | lbcat2 (SCHEMA) | **plan**: getTables(null, null, lbcat2, [TABLE])

# Test: "can snapshot fully qualified table" #

- **connection:** mysql[config:caseInsensitive]

| Permutation | Verified | tableName                                 | OPERATIONS
| :---------- | :------- | :---------------------------------------- | :------
| e83394      | true     | lbcat.4test_table (TABLE)                 | **plan**: getTables(lbcat, null, 4test_table, [TABLE])
| 56b06d      | true     | lbcat.anotherlowertable (TABLE)           | **plan**: getTables(lbcat, null, anotherlowertable, [TABLE])
| ac1d3c      | true     | lbcat.crazy!@#$%^&*()_+{}[]table (TABLE)  | **plan**: getTables(lbcat, null, crazy!@#$%^&*()_+{}[]table, [TABLE])
| e0e1e5      | true     | lbcat.lowertable (TABLE)                  | **plan**: getTables(lbcat, null, lowertable, [TABLE])
| 1217fc      | true     | lbcat.only_in_lbcat (TABLE)               | **plan**: getTables(lbcat, null, only_in_lbcat, [TABLE])
| cdbe33      | true     | lbcat2.4test_table (TABLE)                | **plan**: getTables(lbcat2, null, 4test_table, [TABLE])
| c51a94      | true     | lbcat2.anotherlowertable (TABLE)          | **plan**: getTables(lbcat2, null, anotherlowertable, [TABLE])
| 039ef2      | true     | lbcat2.crazy!@#$%^&*()_+{}[]table (TABLE) | **plan**: getTables(lbcat2, null, crazy!@#$%^&*()_+{}[]table, [TABLE])
| 3cb8b0      | true     | lbcat2.lowertable (TABLE)                 | **plan**: getTables(lbcat2, null, lowertable, [TABLE])
| a1779f      | true     | lbcat2.only_in_lbcat2 (TABLE)             | **plan**: getTables(lbcat2, null, only_in_lbcat2, [TABLE])

# Test Version: "240208" #