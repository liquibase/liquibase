# Test: liquibase.statementlogic.core.SelectTablesLogicTest "tableExists" #

NOTE: This output is generated when the test is ran. DO NOT EDIT MANUALLY

## Permutations ##

- **connection:** Standard DB2 connection

| Permutation | Verified | catalogName | schemaName | tableName  | DETAILS
| 048186      | true     |             |            | table_name | **action**: TablesJdbcMetaDataQueryAction(schemaName=LBUSER, tableName=TABLE_NAME)
| bc8ab2      | true     | LBCAT       |            | table_name | **action**: TablesJdbcMetaDataQueryAction(schemaName=LBCAT, tableName=TABLE_NAME)


## Permutations ##

- **connection:** Standard Sybase ASA connection

| Permutation | Verified               | catalogName | schemaName | tableName  | DETAILS
| 103347      | Connection Unavailable |             |            | table_name | **action**: TablesJdbcMetaDataQueryAction(catalogName=lbcat, schemaName=DBA, tableName=TABLE_NAME)
| 53b900      | Connection Unavailable |             | LBSCHEMA   | table_name | **action**: TablesJdbcMetaDataQueryAction(catalogName=lbcat, schemaName=LBSCHEMA, tableName=TABLE_NAME)
| 7bdced      | Connection Unavailable |             | LBSCHEMA2  | table_name | **action**: TablesJdbcMetaDataQueryAction(catalogName=lbcat, schemaName=LBSCHEMA2, tableName=TABLE_NAME)
| c3f781      | Connection Unavailable | LBCAT       |            | table_name | **action**: TablesJdbcMetaDataQueryAction(catalogName=lbcat, schemaName=DBA, tableName=TABLE_NAME)
| 979bdf      | Connection Unavailable | LBCAT       | LBSCHEMA   | table_name | **action**: TablesJdbcMetaDataQueryAction(catalogName=lbcat, schemaName=LBSCHEMA, tableName=TABLE_NAME)
| 9b787b      | Connection Unavailable | LBCAT       | LBSCHEMA2  | table_name | **action**: TablesJdbcMetaDataQueryAction(catalogName=lbcat, schemaName=LBSCHEMA2, tableName=TABLE_NAME)


## Permutations ##

- **connection:** Standard Firebird connection

| Permutation | Verified               | catalogName | schemaName | tableName  | DETAILS
| 63a99a      | Connection Unavailable |             |            | table_name | **action**: TablesJdbcMetaDataQueryAction(catalogName=LBCAT, schemaName=LBCAT, tableName=TABLE_NAME)


## Permutations ##

- **connection:** Standard H2 connection

| Permutation | Verified | catalogName | schemaName | tableName  | DETAILS
| b2c5b0      | true     |             |            | table_name | **action**: TablesJdbcMetaDataQueryAction(catalogName=LBCAT, schemaName=PUBLIC, tableName=TABLE_NAME)
| d91c8e      | true     |             | LBSCHEMA   | table_name | **action**: TablesJdbcMetaDataQueryAction(catalogName=LBCAT, schemaName=LBSCHEMA, tableName=TABLE_NAME)
| 2693f4      | true     |             | LBSCHEMA2  | table_name | **action**: TablesJdbcMetaDataQueryAction(catalogName=LBCAT, schemaName=LBSCHEMA2, tableName=TABLE_NAME)
| fcd6a7      | true     | LBCAT       |            | table_name | **action**: TablesJdbcMetaDataQueryAction(catalogName=LBCAT, schemaName=PUBLIC, tableName=TABLE_NAME)
| 534228      | true     | LBCAT       | LBSCHEMA   | table_name | **action**: TablesJdbcMetaDataQueryAction(catalogName=LBCAT, schemaName=LBSCHEMA, tableName=TABLE_NAME)
| 8f8d35      | true     | LBCAT       | LBSCHEMA2  | table_name | **action**: TablesJdbcMetaDataQueryAction(catalogName=LBCAT, schemaName=LBSCHEMA2, tableName=TABLE_NAME)


## Permutations ##

- **connection:** Standard Syabase connection

| Permutation | Verified               | catalogName | schemaName | tableName  | DETAILS
| e1df93      | Connection Unavailable |             |            | table_name | **action**: TablesJdbcMetaDataQueryAction(catalogName=lbcat, tableName=TABLE_NAME)
| 098e82      | Connection Unavailable |             | LBSCHEMA   | table_name | **action**: TablesJdbcMetaDataQueryAction(catalogName=lbcat, schemaName=LBSCHEMA, tableName=TABLE_NAME)
| f36c7d      | Connection Unavailable |             | LBSCHEMA2  | table_name | **action**: TablesJdbcMetaDataQueryAction(catalogName=lbcat, schemaName=LBSCHEMA2, tableName=TABLE_NAME)
| ff22e7      | Connection Unavailable | LBCAT       |            | table_name | **action**: TablesJdbcMetaDataQueryAction(catalogName=lbcat, tableName=TABLE_NAME)
| 89c3b6      | Connection Unavailable | LBCAT       | LBSCHEMA   | table_name | **action**: TablesJdbcMetaDataQueryAction(catalogName=lbcat, schemaName=LBSCHEMA, tableName=TABLE_NAME)
| 0577fb      | Connection Unavailable | LBCAT       | LBSCHEMA2  | table_name | **action**: TablesJdbcMetaDataQueryAction(catalogName=lbcat, schemaName=LBSCHEMA2, tableName=TABLE_NAME)


## Permutations ##

- **connection:** Standard MySQL connection

| Permutation | Verified               | catalogName | schemaName | tableName  | DETAILS
| 8a8575      | Connection Unavailable |             |            | table_name | **action**: TablesJdbcMetaDataQueryAction(catalogName=lbcat, schemaName=lbcat, tableName=TABLE_NAME)
| a9dc40      | Connection Unavailable | LBCAT       |            | table_name | **action**: TablesJdbcMetaDataQueryAction(catalogName=lbcat, schemaName=lbcat, tableName=TABLE_NAME)


## Permutations ##

- **connection:** Standard Hsql connection

| Permutation | Verified | catalogName | schemaName | tableName  | DETAILS
| e2f87c      | true     |             |            | table_name | **action**: TablesJdbcMetaDataQueryAction(catalogName=PUBLIC, schemaName=PUBLIC, tableName=TABLE_NAME)
| 5ce3e5      | true     |             | LBSCHEMA   | table_name | **action**: TablesJdbcMetaDataQueryAction(catalogName=PUBLIC, schemaName=LBSCHEMA, tableName=TABLE_NAME)
| 1c1fe0      | true     |             | LBSCHEMA2  | table_name | **action**: TablesJdbcMetaDataQueryAction(catalogName=PUBLIC, schemaName=LBSCHEMA2, tableName=TABLE_NAME)
| b93a06      | true     | PUBLIC      |            | table_name | **action**: TablesJdbcMetaDataQueryAction(catalogName=PUBLIC, schemaName=PUBLIC, tableName=TABLE_NAME)
| 6c8496      | true     | PUBLIC      | LBSCHEMA   | table_name | **action**: TablesJdbcMetaDataQueryAction(catalogName=PUBLIC, schemaName=LBSCHEMA, tableName=TABLE_NAME)
| ed021c      | true     | PUBLIC      | LBSCHEMA2  | table_name | **action**: TablesJdbcMetaDataQueryAction(catalogName=PUBLIC, schemaName=LBSCHEMA2, tableName=TABLE_NAME)


## Permutations ##

- **connection:** Standard MS SqlServer connection

| Permutation | Verified | catalogName | schemaName | tableName  | DETAILS
| 2724b9      | true     |             |            | table_name | **action**: TablesJdbcMetaDataQueryAction(catalogName=lbcat, schemaName=dbo, tableName=TABLE_NAME)
| b3e0f1      | true     |             | LBSCHEMA   | table_name | **action**: TablesJdbcMetaDataQueryAction(catalogName=lbcat, schemaName=lbschema, tableName=TABLE_NAME)
| aac6ec      | true     |             | LBSCHEMA2  | table_name | **action**: TablesJdbcMetaDataQueryAction(catalogName=lbcat, schemaName=lbschema2, tableName=TABLE_NAME)
| 0183ff      | true     | LBCAT       |            | table_name | **action**: TablesJdbcMetaDataQueryAction(catalogName=lbcat, schemaName=dbo, tableName=TABLE_NAME)
| a87c62      | true     | LBCAT       | LBSCHEMA   | table_name | **action**: TablesJdbcMetaDataQueryAction(catalogName=lbcat, schemaName=lbschema, tableName=TABLE_NAME)
| e9a23a      | true     | LBCAT       | LBSCHEMA2  | table_name | **action**: TablesJdbcMetaDataQueryAction(catalogName=lbcat, schemaName=lbschema2, tableName=TABLE_NAME)


## Permutations ##

- **connection:** Standard Oracle connection

| Permutation | Verified | catalogName | schemaName | tableName  | DETAILS
| e9adbe      | true     |             |            | table_name | **action**: TablesJdbcMetaDataQueryAction(schemaName=LBUSER, tableName=TABLE_NAME)
| b98c8a      | true     | LBUSER      |            | table_name | **action**: TablesJdbcMetaDataQueryAction(schemaName=LBUSER, tableName=TABLE_NAME)
| accae9      | true     | LBUSER2     |            | table_name | **action**: TablesJdbcMetaDataQueryAction(schemaName=LBUSER2, tableName=TABLE_NAME)


## Permutations ##

- **connection:** Standard Sqlite connection

| Permutation | Verified               | catalogName | schemaName | tableName  | DETAILS
| 173944      | Connection Unavailable |             |            | table_name | **action**: TablesJdbcMetaDataQueryAction(catalogName=lbcat, schemaName=lbcat, tableName=TABLE_NAME)
| f2f89f      | Connection Unavailable | LBCAT       |            | table_name | **action**: TablesJdbcMetaDataQueryAction(catalogName=lbcat, schemaName=lbcat, tableName=TABLE_NAME)


## Permutations ##

- **connection:** Standard Informix connection

| Permutation | Verified               | catalogName | schemaName | tableName  | DETAILS
| 927c32      | Connection Unavailable |             |            | table_name | **action**: TablesJdbcMetaDataQueryAction(catalogName=lbcat, tableName=TABLE_NAME)
| 890391      | Connection Unavailable |             | LBSCHEMA   | table_name | **action**: TablesJdbcMetaDataQueryAction(catalogName=lbcat, schemaName=LBSCHEMA, tableName=TABLE_NAME)
| 0a734e      | Connection Unavailable |             | LBSCHEMA2  | table_name | **action**: TablesJdbcMetaDataQueryAction(catalogName=lbcat, schemaName=LBSCHEMA2, tableName=TABLE_NAME)
| 373ec9      | Connection Unavailable | LBCAT       |            | table_name | **action**: TablesJdbcMetaDataQueryAction(catalogName=lbcat, tableName=TABLE_NAME)
| 921eaf      | Connection Unavailable | LBCAT       | LBSCHEMA   | table_name | **action**: TablesJdbcMetaDataQueryAction(catalogName=lbcat, schemaName=LBSCHEMA, tableName=TABLE_NAME)
| 01cfb0      | Connection Unavailable | LBCAT       | LBSCHEMA2  | table_name | **action**: TablesJdbcMetaDataQueryAction(catalogName=lbcat, schemaName=LBSCHEMA2, tableName=TABLE_NAME)


## Permutations ##

- **connection:** Standard Derby connection

| Permutation | Verified | catalogName | schemaName | tableName  | DETAILS
| 71eaf7      | true     |             |            | table_name | **action**: TablesJdbcMetaDataQueryAction(schemaName=LBUSER, tableName=TABLE_NAME)
| e43311      | true     | LBCAT       |            | table_name | **action**: TablesJdbcMetaDataQueryAction(schemaName=LBCAT, tableName=TABLE_NAME)


## Permutations ##

- **connection:** Standard PostgreSQL connection

| Permutation | Verified               | catalogName | schemaName | tableName  | DETAILS
| 20060b      | Connection Unavailable |             |            | table_name | **action**: TablesJdbcMetaDataQueryAction(catalogName=lbcat, schemaName=public, tableName=table_name)
| 76ed8d      | Connection Unavailable |             | LBSCHEMA   | table_name | **action**: TablesJdbcMetaDataQueryAction(catalogName=lbcat, schemaName=lbschema, tableName=table_name)
| cc8688      | Connection Unavailable |             | LBSCHEMA2  | table_name | **action**: TablesJdbcMetaDataQueryAction(catalogName=lbcat, schemaName=lbschema2, tableName=table_name)
| 696d04      | Connection Unavailable | LBCAT       |            | table_name | **action**: TablesJdbcMetaDataQueryAction(catalogName=lbcat, schemaName=public, tableName=table_name)
| 1b4592      | Connection Unavailable | LBCAT       | LBSCHEMA   | table_name | **action**: TablesJdbcMetaDataQueryAction(catalogName=lbcat, schemaName=lbschema, tableName=table_name)
| 33be44      | Connection Unavailable | LBCAT       | LBSCHEMA2  | table_name | **action**: TablesJdbcMetaDataQueryAction(catalogName=lbcat, schemaName=lbschema2, tableName=table_name)


