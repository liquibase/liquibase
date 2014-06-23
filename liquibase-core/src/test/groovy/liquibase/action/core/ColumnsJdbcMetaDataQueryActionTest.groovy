package liquibase.action.core

import liquibase.RuntimeEnvironment
import liquibase.database.jvm.JdbcConnection
import liquibase.executor.ExecutionOptions
import liquibase.sdk.database.MockDatabase
import liquibase.sdk.database.MockResultSet
import liquibase.structure.core.Column
import spock.lang.Specification
import spock.lang.Unroll

import java.sql.DatabaseMetaData

class ColumnsJdbcMetaDataQueryActionTest extends Specification {

    @Unroll
    def "getRawMetaData"() {
        when:
        def action = new ColumnsJdbcMetaDataQueryAction(catalogName, schemaName, tableName, columnName)
        def database = new MockDatabase()
        def connection = Mock(JdbcConnection)
        def metaData = Mock(DatabaseMetaData)

        connection.getMetaData() >> metaData
        1 * metaData.getColumns(catalogName, schemaName, tableName, columnName) >> new MockResultSet()
        database.setConnection(connection)

        then:
        action.getRawMetaData(new ExecutionOptions(new RuntimeEnvironment(database)))

        where:
        catalogName | schemaName    | tableName    | columnName
        "cat_name"  | "schema_name" | "table_name" | "col_name"
        "cat_name"  | "schema_name" | null         | "col_name"
        "cat_name"  | "schema_name" | "table_name" | null
        "cat_name"  | "schema_name" | null         | null
    }

    def "rawMetaDataToObject autoincrement int from mysql"() {
        when:
        def action = new ColumnsJdbcMetaDataQueryAction(null, null, null, null)
        def Column column = action.rawMetaDataToObject([
                TABLE_NAME        : "account",
                COLUMN_DEF        : "null",
                CHAR_OCTET_LENGTH : "null",
                SQL_DATETIME_SUB  : 0,
                REMARKS           : "",
                SCOPE_SCHEMA      : "null",
                TABLE_SCHEM       : "null",
                BUFFER_LENGTH     : 65535,
                NULLABLE          : 0,
                IS_NULLABLE       : "NO",
                SQL_DATA_TYPE     : 0,
                TABLE_CAT         : "lbcat",
                NUM_PREC_RADIX    : 10,
                COLUMN_SIZE       : 10,
                TYPE_NAME         : "INT",
                IS_AUTOINCREMENT  : "YES",
                COLUMN_NAME       : "id",
                SCOPE_CATALOG     : "null",
                ORDINAL_POSITION  : 1,
                SCOPE_TABLE       : "null",
                SOURCE_DATA_TYPE  : "null",
                DECIMAL_DIGITS    : 0,
                DATA_TYPE         : 4,
                IS_GENERATEDCOLUMN: ""
        ], options)

        then:
        column.getName() == "id"
        column.getRelation().getName() == "account"
        column.getSchema().getCatalogName() == "lbcat"
        column.getPosition() == 1
        column.getRemarks() == null
        assert !column.isNullable()
        assert column.isAutoIncrement()
    }

    def "rawMetaDataToObject varchar from mysql"() {
        when:
        def action = new ColumnsJdbcMetaDataQueryAction(null, null, null, null)
        def Column column = action.rawMetaDataToObject([
                TABLE_NAME        : "account",
                COLUMN_DEF        : null,
                CHAR_OCTET_LENGTH : 20,
                SQL_DATETIME_SUB  : 0,
                REMARKS           : "test column remarks",
                SCOPE_SCHEMA      : null,
                TABLE_SCHEM       : null,
                BUFFER_LENGTH     : 65535,
                NULLABLE          : 1,
                IS_NULLABLE       : "YES",
                SQL_DATA_TYPE     : 0,
                TABLE_CAT         : "lbcat",
                NUM_PREC_RADIX    : 10,
                COLUMN_SIZE       : 20,
                TYPE_NAME         : "VARCHAR",
                IS_AUTOINCREMENT  : "NO",
                COLUMN_NAME       : "username",
                SCOPE_CATALOG     : null,
                ORDINAL_POSITION  : 2,
                SCOPE_TABLE       : null,
                SOURCE_DATA_TYPE  : null,
                DECIMAL_DIGITS    : null,
                DATA_TYPE         : 12,
                IS_GENERATEDCOLUMN: ""
        ], options)

        then:
        column.getName() == "username"
        column.getRelation().getName() == "account"
        column.getSchema().getCatalogName() == "lbcat"
        column.getPosition() == 2
        column.getRemarks() == "test column remarks"
        assert column.isNullable()
        assert !column.isAutoIncrement()
    }



}
