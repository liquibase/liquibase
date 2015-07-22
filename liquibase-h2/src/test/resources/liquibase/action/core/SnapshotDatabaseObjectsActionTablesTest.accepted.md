**NOTE: This output is generated and parsed by TestMD. Please read it, but DO NOT EDIT MANUALLY**

# Test: "can snapshot all tables in schema" #

- **connection:** h2[config:standard]

| Permutation | Verified | schemaName | OPERATIONS
| :---------- | :------- | :--------- | :------
| 679812      | true     | LBSCHEMA2  | **plan**: getTables(null, LBSCHEMA2, null, [TABLE])
| bd13a9      | true     | PUBLIC     | **plan**: getTables(null, PUBLIC, null, [TABLE])

# Test: "can snapshot all tables in schema with a null table name" #

- **connection:** h2[config:standard]

| Permutation | Verified | schemaName | OPERATIONS
| :---------- | :------- | :--------- | :------
| 679812      | true     | LBSCHEMA2  | **plan**: getTables(null, LBSCHEMA2, null, [TABLE])
| bd13a9      | true     | PUBLIC     | **plan**: getTables(null, PUBLIC, null, [TABLE])

# Test: "can snapshot fully qualified table" #

- **connection:** h2[config:standard]

| Permutation | Verified | tableName                                  | OPERATIONS
| :---------- | :------- | :----------------------------------------- | :------
| c13c6e      | true     | Table LBSCHEMA2.4TEST_table                | **plan**: getTables(null, LBSCHEMA2, 4TEST_table, [TABLE])
| 389b02      | true     | Table LBSCHEMA2.4test_table                | **plan**: getTables(null, LBSCHEMA2, 4test_table, [TABLE])
| 1409f6      | true     | Table LBSCHEMA2.ANOTHERUPPERTABLE          | **plan**: getTables(null, LBSCHEMA2, ANOTHERUPPERTABLE, [TABLE])
| c127f7      | true     | Table LBSCHEMA2.AnotherMixedTable          | **plan**: getTables(null, LBSCHEMA2, AnotherMixedTable, [TABLE])
| 22883c      | true     | Table LBSCHEMA2.CRAZY!@#$%^&*()_+{}[]TABLE | **plan**: getTables(null, LBSCHEMA2, CRAZY!@#$%^&*()_+{}[]TABLE, [TABLE])
| 0c8deb      | true     | Table LBSCHEMA2.MixedTable                 | **plan**: getTables(null, LBSCHEMA2, MixedTable, [TABLE])
| 05571b      | true     | Table LBSCHEMA2.UPPERTABLE                 | **plan**: getTables(null, LBSCHEMA2, UPPERTABLE, [TABLE])
| 5ad093      | true     | Table LBSCHEMA2.anotherlowertable          | **plan**: getTables(null, LBSCHEMA2, anotherlowertable, [TABLE])
| f583af      | true     | Table LBSCHEMA2.crazy!@#$%^&*()_+{}[]table | **plan**: getTables(null, LBSCHEMA2, crazy!@#$%^&*()_+{}[]table, [TABLE])
| 133751      | true     | Table LBSCHEMA2.lowertable                 | **plan**: getTables(null, LBSCHEMA2, lowertable, [TABLE])
| b43347      | true     | Table LBSCHEMA2.only_in_LBSCHEMA2          | **plan**: getTables(null, LBSCHEMA2, only_in_LBSCHEMA2, [TABLE])
| 42d0ab      | true     | Table PUBLIC.4TEST_table                   | **plan**: getTables(null, PUBLIC, 4TEST_table, [TABLE])
| 910473      | true     | Table PUBLIC.4test_table                   | **plan**: getTables(null, PUBLIC, 4test_table, [TABLE])
| 40fa25      | true     | Table PUBLIC.ANOTHERUPPERTABLE             | **plan**: getTables(null, PUBLIC, ANOTHERUPPERTABLE, [TABLE])
| 2a39f3      | true     | Table PUBLIC.AnotherMixedTable             | **plan**: getTables(null, PUBLIC, AnotherMixedTable, [TABLE])
| db9a6b      | true     | Table PUBLIC.CRAZY!@#$%^&*()_+{}[]TABLE    | **plan**: getTables(null, PUBLIC, CRAZY!@#$%^&*()_+{}[]TABLE, [TABLE])
| ac50c1      | true     | Table PUBLIC.MixedTable                    | **plan**: getTables(null, PUBLIC, MixedTable, [TABLE])
| 71d072      | true     | Table PUBLIC.UPPERTABLE                    | **plan**: getTables(null, PUBLIC, UPPERTABLE, [TABLE])
| 426d70      | true     | Table PUBLIC.anotherlowertable             | **plan**: getTables(null, PUBLIC, anotherlowertable, [TABLE])
| a7eb54      | true     | Table PUBLIC.crazy!@#$%^&*()_+{}[]table    | **plan**: getTables(null, PUBLIC, crazy!@#$%^&*()_+{}[]table, [TABLE])
| 0429b4      | true     | Table PUBLIC.lowertable                    | **plan**: getTables(null, PUBLIC, lowertable, [TABLE])
| 4e02e7      | true     | Table PUBLIC.only_in_PUBLIC                | **plan**: getTables(null, PUBLIC, only_in_PUBLIC, [TABLE])

# Test: "can snapshot tables related to a catalog" #

## Permutation 01873a (verified) ##

- **connection:** h2[config:standard]

#### Results ####

- **plan:** getTables(null, null, null, [TABLE])

# Test: "can snapshot tables related to a schema" #

- **connection:** h2[config:standard]

| Permutation | Verified | schemaName | OPERATIONS
| :---------- | :------- | :--------- | :------
| 679812      | true     | LBSCHEMA2  | **plan**: getTables(null, LBSCHEMA2, null, [TABLE])
| bd13a9      | true     | PUBLIC     | **plan**: getTables(null, PUBLIC, null, [TABLE])
