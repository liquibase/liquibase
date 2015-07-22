**NOTE: This output is generated and parsed by TestMD. Please read it, but DO NOT EDIT MANUALLY**

# Test: "can snapshot all tables in schema" #

- **connection:** unsupported[config:standard]

| Permutation | Verified             | schemaName | OPERATIONS
| :---------- | :------------------- | :--------- | :------
| 352224      | Unsupported Database | LBSCHEMA   | **plan**: getTables(null, LBSCHEMA, null, [TABLE])
| ca1f3e      | Unsupported Database | LBSCHEMA2  | **plan**: getTables(null, LBSCHEMA2, null, [TABLE])

# Test: "can snapshot all tables in schema with a null table name" #

- **connection:** unsupported[config:standard]

| Permutation | Verified             | schemaName | OPERATIONS
| :---------- | :------------------- | :--------- | :------
| 352224      | Unsupported Database | LBSCHEMA   | **plan**: getTables(null, LBSCHEMA, null, [TABLE])
| ca1f3e      | Unsupported Database | LBSCHEMA2  | **plan**: getTables(null, LBSCHEMA2, null, [TABLE])

# Test: "can snapshot fully qualified table" #

- **connection:** unsupported[config:standard]

| Permutation | Verified             | tableName                                  | OPERATIONS
| :---------- | :------------------- | :----------------------------------------- | :------
| 1c8830      | Unsupported Database | Table LBSCHEMA.4TEST_table                 | **plan**: getTables(null, LBSCHEMA, 4TEST_table, [TABLE])
| 28b057      | Unsupported Database | Table LBSCHEMA.4test_table                 | **plan**: getTables(null, LBSCHEMA, 4test_table, [TABLE])
| 721bc5      | Unsupported Database | Table LBSCHEMA.ANOTHERUPPERTABLE           | **plan**: getTables(null, LBSCHEMA, ANOTHERUPPERTABLE, [TABLE])
| 37ade9      | Unsupported Database | Table LBSCHEMA.AnotherMixedTable           | **plan**: getTables(null, LBSCHEMA, AnotherMixedTable, [TABLE])
| 0f2aaa      | Unsupported Database | Table LBSCHEMA.CRAZY!@#$%^&*()_+{}[]TABLE  | **plan**: getTables(null, LBSCHEMA, CRAZY!@#$%^&*()_+{}[]TABLE, [TABLE])
| f9669a      | Unsupported Database | Table LBSCHEMA.MixedTable                  | **plan**: getTables(null, LBSCHEMA, MixedTable, [TABLE])
| 85612b      | Unsupported Database | Table LBSCHEMA.UPPERTABLE                  | **plan**: getTables(null, LBSCHEMA, UPPERTABLE, [TABLE])
| c9649d      | Unsupported Database | Table LBSCHEMA.anotherlowertable           | **plan**: getTables(null, LBSCHEMA, anotherlowertable, [TABLE])
| e2f777      | Unsupported Database | Table LBSCHEMA.crazy!@#$%^&*()_+{}[]table  | **plan**: getTables(null, LBSCHEMA, crazy!@#$%^&*()_+{}[]table, [TABLE])
| ad8259      | Unsupported Database | Table LBSCHEMA.lowertable                  | **plan**: getTables(null, LBSCHEMA, lowertable, [TABLE])
| b1196b      | Unsupported Database | Table LBSCHEMA.only_in_LBSCHEMA            | **plan**: getTables(null, LBSCHEMA, only_in_LBSCHEMA, [TABLE])
| 1adb66      | Unsupported Database | Table LBSCHEMA2.4TEST_table                | **plan**: getTables(null, LBSCHEMA2, 4TEST_table, [TABLE])
| 978ba2      | Unsupported Database | Table LBSCHEMA2.4test_table                | **plan**: getTables(null, LBSCHEMA2, 4test_table, [TABLE])
| c83e0f      | Unsupported Database | Table LBSCHEMA2.ANOTHERUPPERTABLE          | **plan**: getTables(null, LBSCHEMA2, ANOTHERUPPERTABLE, [TABLE])
| 264771      | Unsupported Database | Table LBSCHEMA2.AnotherMixedTable          | **plan**: getTables(null, LBSCHEMA2, AnotherMixedTable, [TABLE])
| 4a6b91      | Unsupported Database | Table LBSCHEMA2.CRAZY!@#$%^&*()_+{}[]TABLE | **plan**: getTables(null, LBSCHEMA2, CRAZY!@#$%^&*()_+{}[]TABLE, [TABLE])
| 508dd9      | Unsupported Database | Table LBSCHEMA2.MixedTable                 | **plan**: getTables(null, LBSCHEMA2, MixedTable, [TABLE])
| a420e7      | Unsupported Database | Table LBSCHEMA2.UPPERTABLE                 | **plan**: getTables(null, LBSCHEMA2, UPPERTABLE, [TABLE])
| 54fd48      | Unsupported Database | Table LBSCHEMA2.anotherlowertable          | **plan**: getTables(null, LBSCHEMA2, anotherlowertable, [TABLE])
| fa1907      | Unsupported Database | Table LBSCHEMA2.crazy!@#$%^&*()_+{}[]table | **plan**: getTables(null, LBSCHEMA2, crazy!@#$%^&*()_+{}[]table, [TABLE])
| d60f61      | Unsupported Database | Table LBSCHEMA2.lowertable                 | **plan**: getTables(null, LBSCHEMA2, lowertable, [TABLE])
| 1e3850      | Unsupported Database | Table LBSCHEMA2.only_in_LBSCHEMA2          | **plan**: getTables(null, LBSCHEMA2, only_in_LBSCHEMA2, [TABLE])

# Test: "can snapshot tables related to a catalog" #

## Permutation 854d86 _NOT VERIFIED: Unsupported Database_ ##

- **connection:** unsupported[config:standard]

#### Results ####

- **plan:** getTables(null, null, null, [TABLE])

# Test: "can snapshot tables related to a schema" #

- **connection:** unsupported[config:standard]

| Permutation | Verified             | schemaName | OPERATIONS
| :---------- | :------------------- | :--------- | :------
| 352224      | Unsupported Database | LBSCHEMA   | **plan**: getTables(null, LBSCHEMA, null, [TABLE])
| ca1f3e      | Unsupported Database | LBSCHEMA2  | **plan**: getTables(null, LBSCHEMA2, null, [TABLE])
