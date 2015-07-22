**NOTE: This output is generated and parsed by TestMD. Please read it, but DO NOT EDIT MANUALLY**

# Test: "autoIncrement information set correctly" #

- **connection:** mysql[config:caseInsensitive]

| Permutation | Verified | autoIncrement | columnName               | OPERATIONS
| :---------- | :------- | :------------ | :----------------------- | :------
| a121d6      | true     | false         | LBSCHEMA.table1.column1  | **plan**: getColumns(LBSCHEMA, null, table1, column1)
| 28db1d      | true     | false         | LBSCHEMA2.table1.column1 | **plan**: getColumns(LBSCHEMA2, null, table1, column1)
| 14cbf8      | true     | true          | LBSCHEMA.table1.column1  | **plan**: getColumns(LBSCHEMA, null, table1, column1)
| fe04d2      | true     | true          | LBSCHEMA2.table1.column1 | **plan**: getColumns(LBSCHEMA2, null, table1, column1)

# Test: "can find all columns in a fully qualified complex table name" #

- **connection:** mysql[config:caseInsensitive]

| Permutation | Verified | tableName                            | OPERATIONS
| :---------- | :------- | :----------------------------------- | :------
| 891215      | true     | LBSCHEMA.4test_table                 | **plan**: getColumns(LBSCHEMA, null, 4test_table, null)
| 379a2e      | true     | LBSCHEMA.anotherlowertable           | **plan**: getColumns(LBSCHEMA, null, anotherlowertable, null)
| eef535      | true     | LBSCHEMA.crazy!@#$%^&*()_+{}[]table  | **plan**: getColumns(LBSCHEMA, null, crazy!@#$%^&*()_+{}[]table, null)
| 4f26e4      | true     | LBSCHEMA.lowertable                  | **plan**: getColumns(LBSCHEMA, null, lowertable, null)
| b70924      | true     | LBSCHEMA2.4test_table                | **plan**: getColumns(LBSCHEMA2, null, 4test_table, null)
| ccca38      | true     | LBSCHEMA2.anotherlowertable          | **plan**: getColumns(LBSCHEMA2, null, anotherlowertable, null)
| fa3b58      | true     | LBSCHEMA2.crazy!@#$%^&*()_+{}[]table | **plan**: getColumns(LBSCHEMA2, null, crazy!@#$%^&*()_+{}[]table, null)
| 7bb4b4      | true     | LBSCHEMA2.lowertable                 | **plan**: getColumns(LBSCHEMA2, null, lowertable, null)

# Test: "can find all columns in a schema" #

- **connection:** mysql[config:caseInsensitive]

| Permutation | Verified | schemaName | OPERATIONS
| :---------- | :------- | :--------- | :------
| 020de5      | true     | LBSCHEMA   | **plan**: getColumns(LBSCHEMA, null, null, null)
| 6d6ef2      | true     | LBSCHEMA2  | **plan**: getColumns(LBSCHEMA2, null, null, null)

# Test: "can find fully qualified complex column names" #

- **connection:** mysql[config:caseInsensitive]

| Permutation | Verified | columnName                                                       | OPERATIONS
| :---------- | :------- | :--------------------------------------------------------------- | :------
| f716ec      | true     | LBSCHEMA.4test_table.4test_column                                | **plan**: getColumns(LBSCHEMA, null, 4test_table, 4test_column)
| c2b510      | true     | LBSCHEMA.4test_table.anotherlowercolumn                          | **plan**: getColumns(LBSCHEMA, null, 4test_table, anotherlowercolumn)
| ab8646      | true     | LBSCHEMA.4test_table.crazy!@#$%^&*()_+{}[]column                 | **plan**: getColumns(LBSCHEMA, null, 4test_table, crazy!@#$%^&*()_+{}[]column)
| 34c444      | true     | LBSCHEMA.4test_table.lowercolumn                                 | **plan**: getColumns(LBSCHEMA, null, 4test_table, lowercolumn)
| 1637ef      | true     | LBSCHEMA.4test_table.only_in_null                                | **plan**: getColumns(LBSCHEMA, null, 4test_table, only_in_null)
| 7f1854      | true     | LBSCHEMA.anotherlowertable.4test_column                          | **plan**: getColumns(LBSCHEMA, null, anotherlowertable, 4test_column)
| 4823fa      | true     | LBSCHEMA.anotherlowertable.anotherlowercolumn                    | **plan**: getColumns(LBSCHEMA, null, anotherlowertable, anotherlowercolumn)
| 191c7a      | true     | LBSCHEMA.anotherlowertable.crazy!@#$%^&*()_+{}[]column           | **plan**: getColumns(LBSCHEMA, null, anotherlowertable, crazy!@#$%^&*()_+{}[]column)
| 416dbb      | true     | LBSCHEMA.anotherlowertable.lowercolumn                           | **plan**: getColumns(LBSCHEMA, null, anotherlowertable, lowercolumn)
| 400934      | true     | LBSCHEMA.anotherlowertable.only_in_null                          | **plan**: getColumns(LBSCHEMA, null, anotherlowertable, only_in_null)
| c5760a      | true     | LBSCHEMA.crazy!@#$%^&*()_+{}[]table.4test_column                 | **plan**: getColumns(LBSCHEMA, null, crazy!@#$%^&*()_+{}[]table, 4test_column)
| 80fb0a      | true     | LBSCHEMA.crazy!@#$%^&*()_+{}[]table.anotherlowercolumn           | **plan**: getColumns(LBSCHEMA, null, crazy!@#$%^&*()_+{}[]table, anotherlowercolumn)
| 75f9be      | true     | LBSCHEMA.crazy!@#$%^&*()_+{}[]table.crazy!@#$%^&*()_+{}[]column  | **plan**: getColumns(LBSCHEMA, null, crazy!@#$%^&*()_+{}[]table, crazy!@#$%^&*()_+{}[]column)
| 1495f8      | true     | LBSCHEMA.crazy!@#$%^&*()_+{}[]table.lowercolumn                  | **plan**: getColumns(LBSCHEMA, null, crazy!@#$%^&*()_+{}[]table, lowercolumn)
| 4271e9      | true     | LBSCHEMA.crazy!@#$%^&*()_+{}[]table.only_in_null                 | **plan**: getColumns(LBSCHEMA, null, crazy!@#$%^&*()_+{}[]table, only_in_null)
| 846372      | true     | LBSCHEMA.lowertable.4test_column                                 | **plan**: getColumns(LBSCHEMA, null, lowertable, 4test_column)
| f08518      | true     | LBSCHEMA.lowertable.anotherlowercolumn                           | **plan**: getColumns(LBSCHEMA, null, lowertable, anotherlowercolumn)
| 1fd760      | true     | LBSCHEMA.lowertable.crazy!@#$%^&*()_+{}[]column                  | **plan**: getColumns(LBSCHEMA, null, lowertable, crazy!@#$%^&*()_+{}[]column)
| 8d5606      | true     | LBSCHEMA.lowertable.lowercolumn                                  | **plan**: getColumns(LBSCHEMA, null, lowertable, lowercolumn)
| 16f864      | true     | LBSCHEMA.lowertable.only_in_null                                 | **plan**: getColumns(LBSCHEMA, null, lowertable, only_in_null)
| e5af77      | true     | LBSCHEMA2.4test_table.4test_column                               | **plan**: getColumns(LBSCHEMA2, null, 4test_table, 4test_column)
| 22cd9e      | true     | LBSCHEMA2.4test_table.anotherlowercolumn                         | **plan**: getColumns(LBSCHEMA2, null, 4test_table, anotherlowercolumn)
| 004e8a      | true     | LBSCHEMA2.4test_table.crazy!@#$%^&*()_+{}[]column                | **plan**: getColumns(LBSCHEMA2, null, 4test_table, crazy!@#$%^&*()_+{}[]column)
| 83d61a      | true     | LBSCHEMA2.4test_table.lowercolumn                                | **plan**: getColumns(LBSCHEMA2, null, 4test_table, lowercolumn)
| e627b0      | true     | LBSCHEMA2.4test_table.only_in_null                               | **plan**: getColumns(LBSCHEMA2, null, 4test_table, only_in_null)
| ce11a7      | true     | LBSCHEMA2.anotherlowertable.4test_column                         | **plan**: getColumns(LBSCHEMA2, null, anotherlowertable, 4test_column)
| 310dfb      | true     | LBSCHEMA2.anotherlowertable.anotherlowercolumn                   | **plan**: getColumns(LBSCHEMA2, null, anotherlowertable, anotherlowercolumn)
| 414253      | true     | LBSCHEMA2.anotherlowertable.crazy!@#$%^&*()_+{}[]column          | **plan**: getColumns(LBSCHEMA2, null, anotherlowertable, crazy!@#$%^&*()_+{}[]column)
| 9e6805      | true     | LBSCHEMA2.anotherlowertable.lowercolumn                          | **plan**: getColumns(LBSCHEMA2, null, anotherlowertable, lowercolumn)
| d35e51      | true     | LBSCHEMA2.anotherlowertable.only_in_null                         | **plan**: getColumns(LBSCHEMA2, null, anotherlowertable, only_in_null)
| 546deb      | true     | LBSCHEMA2.crazy!@#$%^&*()_+{}[]table.4test_column                | **plan**: getColumns(LBSCHEMA2, null, crazy!@#$%^&*()_+{}[]table, 4test_column)
| 1bcf55      | true     | LBSCHEMA2.crazy!@#$%^&*()_+{}[]table.anotherlowercolumn          | **plan**: getColumns(LBSCHEMA2, null, crazy!@#$%^&*()_+{}[]table, anotherlowercolumn)
| ac50ae      | true     | LBSCHEMA2.crazy!@#$%^&*()_+{}[]table.crazy!@#$%^&*()_+{}[]column | **plan**: getColumns(LBSCHEMA2, null, crazy!@#$%^&*()_+{}[]table, crazy!@#$%^&*()_+{}[]column)
| 1668b1      | true     | LBSCHEMA2.crazy!@#$%^&*()_+{}[]table.lowercolumn                 | **plan**: getColumns(LBSCHEMA2, null, crazy!@#$%^&*()_+{}[]table, lowercolumn)
| 432ba7      | true     | LBSCHEMA2.crazy!@#$%^&*()_+{}[]table.only_in_null                | **plan**: getColumns(LBSCHEMA2, null, crazy!@#$%^&*()_+{}[]table, only_in_null)
| a741aa      | true     | LBSCHEMA2.lowertable.4test_column                                | **plan**: getColumns(LBSCHEMA2, null, lowertable, 4test_column)
| 815c5b      | true     | LBSCHEMA2.lowertable.anotherlowercolumn                          | **plan**: getColumns(LBSCHEMA2, null, lowertable, anotherlowercolumn)
| 6ae818      | true     | LBSCHEMA2.lowertable.crazy!@#$%^&*()_+{}[]column                 | **plan**: getColumns(LBSCHEMA2, null, lowertable, crazy!@#$%^&*()_+{}[]column)
| 502666      | true     | LBSCHEMA2.lowertable.lowercolumn                                 | **plan**: getColumns(LBSCHEMA2, null, lowertable, lowercolumn)
| a6b2d6      | true     | LBSCHEMA2.lowertable.only_in_null                                | **plan**: getColumns(LBSCHEMA2, null, lowertable, only_in_null)

# Test: "dataType comes through correctly" #

- **connection:** mysql[config:caseInsensitive]

| Permutation | Verified | columnName               | type        | OPERATIONS
| :---------- | :------- | :----------------------- | :---------- | :------
| 2fc5db      | true     | LBSCHEMA2.table1.column1 | bigint      | **plan**: getColumns(LBSCHEMA2, null, table1, column1)
| bee22b      | true     | LBSCHEMA2.table1.column1 | double      | **plan**: getColumns(LBSCHEMA2, null, table1, column1)
| 0146fa      | true     | LBSCHEMA2.table1.column1 | float       | **plan**: getColumns(LBSCHEMA2, null, table1, column1)
| 893e0a      | true     | LBSCHEMA2.table1.column1 | int         | **plan**: getColumns(LBSCHEMA2, null, table1, column1)
| 11d134      | true     | LBSCHEMA2.table1.column1 | smallint    | **plan**: getColumns(LBSCHEMA2, null, table1, column1)
| 858d60      | true     | LBSCHEMA2.table1.column1 | varchar(10) | **plan**: getColumns(LBSCHEMA2, null, table1, column1)
