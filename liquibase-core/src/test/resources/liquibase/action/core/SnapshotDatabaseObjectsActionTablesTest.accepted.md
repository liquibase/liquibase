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

| Permutation | Verified             | tableName                                   | OPERATIONS
| :---------- | :------------------- | :------------------------------------------ | :------
| 47700c      | Unsupported Database | LBCAT.LBSCHEMA.4TEST_table                  | **plan**: getTables(null, null, null, [TABLE])
| 2f8017      | Unsupported Database | LBCAT.LBSCHEMA.4test_table                  | **plan**: getTables(null, null, null, [TABLE])
| a81ee0      | Unsupported Database | LBCAT.LBSCHEMA.ANOTHERUPPERTABLE            | **plan**: getTables(null, null, null, [TABLE])
| 3d37ed      | Unsupported Database | LBCAT.LBSCHEMA.AnotherMixedTable            | **plan**: getTables(null, null, null, [TABLE])
| 8f7451      | Unsupported Database | LBCAT.LBSCHEMA.CRAZY!@#$%^&*()_+{}[]TABLE   | **plan**: getTables(null, null, null, [TABLE])
| 23a0d6      | Unsupported Database | LBCAT.LBSCHEMA.MixedTable                   | **plan**: getTables(null, null, null, [TABLE])
| 56615a      | Unsupported Database | LBCAT.LBSCHEMA.UPPERTABLE                   | **plan**: getTables(null, null, null, [TABLE])
| d82e7a      | Unsupported Database | LBCAT.LBSCHEMA.anotherlowertable            | **plan**: getTables(null, null, null, [TABLE])
| 87827e      | Unsupported Database | LBCAT.LBSCHEMA.crazy!@#$%^&*()_+{}[]table   | **plan**: getTables(null, null, null, [TABLE])
| 92129f      | Unsupported Database | LBCAT.LBSCHEMA.lowertable                   | **plan**: getTables(null, null, null, [TABLE])
| 4d59ef      | Unsupported Database | LBCAT.LBSCHEMA.only_in_LBSCHEMA             | **plan**: getTables(null, null, null, [TABLE])
| 05bfdd      | Unsupported Database | LBCAT.LBSCHEMA2.4TEST_table                 | **plan**: getTables(null, null, null, [TABLE])
| 35194e      | Unsupported Database | LBCAT.LBSCHEMA2.4test_table                 | **plan**: getTables(null, null, null, [TABLE])
| ed32d6      | Unsupported Database | LBCAT.LBSCHEMA2.ANOTHERUPPERTABLE           | **plan**: getTables(null, null, null, [TABLE])
| 21321a      | Unsupported Database | LBCAT.LBSCHEMA2.AnotherMixedTable           | **plan**: getTables(null, null, null, [TABLE])
| fef01a      | Unsupported Database | LBCAT.LBSCHEMA2.CRAZY!@#$%^&*()_+{}[]TABLE  | **plan**: getTables(null, null, null, [TABLE])
| a7393b      | Unsupported Database | LBCAT.LBSCHEMA2.MixedTable                  | **plan**: getTables(null, null, null, [TABLE])
| fb05f8      | Unsupported Database | LBCAT.LBSCHEMA2.UPPERTABLE                  | **plan**: getTables(null, null, null, [TABLE])
| 91df88      | Unsupported Database | LBCAT.LBSCHEMA2.anotherlowertable           | **plan**: getTables(null, null, null, [TABLE])
| 67b206      | Unsupported Database | LBCAT.LBSCHEMA2.crazy!@#$%^&*()_+{}[]table  | **plan**: getTables(null, null, null, [TABLE])
| fb39fa      | Unsupported Database | LBCAT.LBSCHEMA2.lowertable                  | **plan**: getTables(null, null, null, [TABLE])
| ed1ac6      | Unsupported Database | LBCAT.LBSCHEMA2.only_in_LBSCHEMA2           | **plan**: getTables(null, null, null, [TABLE])
| 06fba8      | Unsupported Database | LBCAT2.LBSCHEMA.4TEST_table                 | **plan**: getTables(null, null, null, [TABLE])
| 169a78      | Unsupported Database | LBCAT2.LBSCHEMA.4test_table                 | **plan**: getTables(null, null, null, [TABLE])
| 590dbb      | Unsupported Database | LBCAT2.LBSCHEMA.ANOTHERUPPERTABLE           | **plan**: getTables(null, null, null, [TABLE])
| 52e6af      | Unsupported Database | LBCAT2.LBSCHEMA.AnotherMixedTable           | **plan**: getTables(null, null, null, [TABLE])
| 536b28      | Unsupported Database | LBCAT2.LBSCHEMA.CRAZY!@#$%^&*()_+{}[]TABLE  | **plan**: getTables(null, null, null, [TABLE])
| 088d45      | Unsupported Database | LBCAT2.LBSCHEMA.MixedTable                  | **plan**: getTables(null, null, null, [TABLE])
| 7f6851      | Unsupported Database | LBCAT2.LBSCHEMA.UPPERTABLE                  | **plan**: getTables(null, null, null, [TABLE])
| 8b8196      | Unsupported Database | LBCAT2.LBSCHEMA.anotherlowertable           | **plan**: getTables(null, null, null, [TABLE])
| 58a583      | Unsupported Database | LBCAT2.LBSCHEMA.crazy!@#$%^&*()_+{}[]table  | **plan**: getTables(null, null, null, [TABLE])
| 504bdf      | Unsupported Database | LBCAT2.LBSCHEMA.lowertable                  | **plan**: getTables(null, null, null, [TABLE])
| 62c7dd      | Unsupported Database | LBCAT2.LBSCHEMA.only_in_LBSCHEMA            | **plan**: getTables(null, null, null, [TABLE])
| 322a4d      | Unsupported Database | LBCAT2.LBSCHEMA2.4TEST_table                | **plan**: getTables(null, null, null, [TABLE])
| 71398e      | Unsupported Database | LBCAT2.LBSCHEMA2.4test_table                | **plan**: getTables(null, null, null, [TABLE])
| 59f9c4      | Unsupported Database | LBCAT2.LBSCHEMA2.ANOTHERUPPERTABLE          | **plan**: getTables(null, null, null, [TABLE])
| 8e4282      | Unsupported Database | LBCAT2.LBSCHEMA2.AnotherMixedTable          | **plan**: getTables(null, null, null, [TABLE])
| 5935dd      | Unsupported Database | LBCAT2.LBSCHEMA2.CRAZY!@#$%^&*()_+{}[]TABLE | **plan**: getTables(null, null, null, [TABLE])
| 5dafdd      | Unsupported Database | LBCAT2.LBSCHEMA2.MixedTable                 | **plan**: getTables(null, null, null, [TABLE])
| 31748b      | Unsupported Database | LBCAT2.LBSCHEMA2.UPPERTABLE                 | **plan**: getTables(null, null, null, [TABLE])
| 3fba48      | Unsupported Database | LBCAT2.LBSCHEMA2.anotherlowertable          | **plan**: getTables(null, null, null, [TABLE])
| e98219      | Unsupported Database | LBCAT2.LBSCHEMA2.crazy!@#$%^&*()_+{}[]table | **plan**: getTables(null, null, null, [TABLE])
| b498d6      | Unsupported Database | LBCAT2.LBSCHEMA2.lowertable                 | **plan**: getTables(null, null, null, [TABLE])
| 3575c7      | Unsupported Database | LBCAT2.LBSCHEMA2.only_in_LBSCHEMA2          | **plan**: getTables(null, null, null, [TABLE])

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
