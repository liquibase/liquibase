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

| Permutation | Verified | tableName                                  | OPERATIONS
| :---------- | :------- | :----------------------------------------- | :------
| 2ba435      | true     | Table LBSCHEMA.4test_table                 | **plan**: getTables(LBSCHEMA, null, 4test_table, [TABLE])
| 46d94d      | true     | Table LBSCHEMA.anotherlowertable           | **plan**: getTables(LBSCHEMA, null, anotherlowertable, [TABLE])
| 3a5fa2      | true     | Table LBSCHEMA.crazy!@#$%^&*()_+{}[]table  | **plan**: getTables(LBSCHEMA, null, crazy!@#$%^&*()_+{}[]table, [TABLE])
| 7fc748      | true     | Table LBSCHEMA.lowertable                  | **plan**: getTables(LBSCHEMA, null, lowertable, [TABLE])
| 7daf6c      | true     | Table LBSCHEMA2.4test_table                | **plan**: getTables(LBSCHEMA2, null, 4test_table, [TABLE])
| 074ac6      | true     | Table LBSCHEMA2.anotherlowertable          | **plan**: getTables(LBSCHEMA2, null, anotherlowertable, [TABLE])
| 23927b      | true     | Table LBSCHEMA2.crazy!@#$%^&*()_+{}[]table | **plan**: getTables(LBSCHEMA2, null, crazy!@#$%^&*()_+{}[]table, [TABLE])
| a878b2      | true     | Table LBSCHEMA2.lowertable                 | **plan**: getTables(LBSCHEMA2, null, lowertable, [TABLE])

# Test: "can snapshot tables related to a schema" #

- **connection:** mysql[config:caseInsensitive]

| Permutation | Verified | schemaName | OPERATIONS
| :---------- | :------- | :--------- | :------
| 020de5      | true     | LBSCHEMA   | **plan**: getTables(LBSCHEMA, null, null, [TABLE])
| 6d6ef2      | true     | LBSCHEMA2  | **plan**: getTables(LBSCHEMA2, null, null, [TABLE])
