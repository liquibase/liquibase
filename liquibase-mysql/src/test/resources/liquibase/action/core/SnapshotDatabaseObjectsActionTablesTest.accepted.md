**NOTE: This output is generated and parsed by TestMD. Please read it, but DO NOT EDIT MANUALLY**

# Test: "can snapshot all tables in schema" #

- **connection:** mysql[config:caseInsensitive]

| Permutation | Verified | schemaName | OPERATIONS
| :---------- | :------- | :--------- | :------
| 020de5      | true     | LBSCHEMA   | **plan**: getTables(LBSCHEMA, null, null, [TABLE])
| 6d6ef2      | true     | LBSCHEMA2  | **plan**: getTables(LBSCHEMA2, null, null, [TABLE])

# Test: "can snapshot all tables in schema with a null table name" #

- **connection:** mysql[config:caseInsensitive]

| Permutation | Verified | schemaName | OPERATIONS
| :---------- | :------- | :--------- | :------
| 020de5      | true     | LBSCHEMA   | **plan**: getTables(LBSCHEMA, null, null, [TABLE])
| 6d6ef2      | true     | LBSCHEMA2  | **plan**: getTables(LBSCHEMA2, null, null, [TABLE])

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

# Test: "can snapshot tables related to a schema" #

- **connection:** mysql[config:caseInsensitive]

| Permutation | Verified | schemaName | OPERATIONS
| :---------- | :------- | :--------- | :------
| 020de5      | true     | LBSCHEMA   | **plan**: getTables(LBSCHEMA, null, null, [TABLE])
| 6d6ef2      | true     | LBSCHEMA2  | **plan**: getTables(LBSCHEMA2, null, null, [TABLE])
