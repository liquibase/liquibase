package liquibase.actionlogic.core

import liquibase.JUnitScope
import liquibase.action.core.QueryJdbcMetaDataAction
import liquibase.action.core.SnapshotDatabaseObjectsAction
import liquibase.actionlogic.RowBasedQueryResult
import liquibase.sdk.database.MockDatabase
import liquibase.statement.DatabaseFunction
import liquibase.structure.ObjectName

import liquibase.structure.core.Column
import liquibase.structure.core.DataType
import liquibase.structure.core.Table
import spock.lang.Specification
import spock.lang.Unroll

import java.sql.DatabaseMetaData
import java.sql.Types

class SnapshotColumnsLogicJdbcTest extends Specification {

    @Unroll
    def "convertObject handles column name and table plus container correctly"() {
        when:
        def database = new MockDatabase();
        def scope = JUnitScope.getInstance(database)

        def object = new SnapshotColumnsLogicJdbc().convertToObject(new RowBasedQueryResult.Row([
                TABLE_CAT  : tableCat,
                TABLE_SCHEM: tableSchema,
                TABLE_NAME : tableName,
                COLUMN_NAME: "columnName",
                NULLABLE   : DatabaseMetaData.columnNoNulls,
                DATA_TYPE  : Types.INTEGER,
        ]), new SnapshotDatabaseObjectsAction(Column, new ObjectName(Table.class)), scope)

        then:
        object instanceof Column
        object.name.name.toString() == "columnName"
        object.name.container.toString() == expected

        where:
        tableCat   | tableSchema  | tableName   | expected
        "tableCat" | "schemaName" | "tableName" | "tableCat.schemaName.tableName"
        null       | "schemaName" | "tableName" | "schemaName.tableName"
        "tableCat" | null         | "tableName" | "tableCat.#UNSET.tableName"
        null       | "schemaName" | "tableName" | "schemaName.tableName"
        null       | null         | "tableName" | "tableName"
    }

    @Unroll("#featureName: #expected")
    def "readDataType handles various rows correctly"() {
        when:
        def data = [
                COLUMN_SIZE: columnSize, DATA_TYPE: rowDataType, DECIMAL_DIGITS: decimalDigits, IS_AUTOINCREMENT: isAutoIncrement, TYPE_NAME: typeName, SQL_DATA_TYPE: sqlDataType, CHAR_OCTET_LENGTH: charOctetLength, NUM_PREC_RADIX: numPrecRadix
        ]
        def dataType = new SnapshotColumnsLogicJdbc().readDataType(new RowBasedQueryResult.Row(data), new Column(), JUnitScope.getInstance(new MockDatabase()))

        then:
        dataType.toString() == expected
        dataType.standardType == expectedStandard
        dataType.origin == "mock"

        where:
        expected      | expectedStandard               | columnSize | rowDataType | decimalDigits | isAutoIncrement | typeName   | sqlDataType | charOctetLength | numPrecRadix
        "INTEGER"     | DataType.StandardType.INTEGER  | 10         | 4           | 0             | "NO"            | "INTEGER"  | 4           | 10              | 10
        "BIGINT"      | DataType.StandardType.BIGINT   | 19         | -5          | 0             | "NO"            | "BIGINT"   | -5          | 19              | 10
        "SMALLINT"    | DataType.StandardType.SMALLINT | 5          | 5           | 0             | "NO"            | "SMALLINT" | 5           | 5               | 10
        "VARCHAR(10)" | DataType.StandardType.VARCHAR  | 10         | 12          | 0             | "NO"            | "VARCHAR"  | 12          | 10              | 10
    }

    @Unroll()
    def "readDefaultValue handles various rows correctly"() {
        when:
        def data = [COLUMN_DEF: columnDef]
        def column = new Column(new ObjectName("testTable", "col_name"))
        column.type = dataType

        then:
        new SnapshotColumnsLogicJdbc().readDefaultValue(new RowBasedQueryResult.Row(data), column, JUnitScope.getInstance(new MockDatabase())) == expected

        where:
        columnDef                                                                         | dataType                       | expected
        "(NEXT VALUE FOR LBSCHEMA2.SYSTEM_SEQUENCE_CECABC79_AF80_4314_90F3_40B45A2B2071)" | DataType.parse("bigint")       | new DatabaseFunction("NEXT VALUE FOR LBSCHEMA2.SYSTEM_SEQUENCE_CECABC79_AF80_4314_90F3_40B45A2B2071")
        "(NEXT VALUE FOR LBSCHEMA2.SYSTEM_SEQUENCE_CECABC79_AF80_4314_90F3_40B45A2B2071)" | DataType.parse("int")          | new DatabaseFunction("NEXT VALUE FOR LBSCHEMA2.SYSTEM_SEQUENCE_CECABC79_AF80_4314_90F3_40B45A2B2071")
        "3"                                                                               | DataType.parse("int")          | 3I
        "3"                                                                               | DataType.parse("bigint")       | new BigInteger("3")
        "3"                                                                               | DataType.parse("varchar(10)")  | "3"
        "'test value'"                                                                    | DataType.parse("varchar(10)")  | "test value"
        "'test value'"                                                                    | DataType.parse("nvarchar(10)") | "test value"
        null                                                                              | DataType.parse("int")          | null
        null                                                                              | DataType.parse("varchar(10)")  | null
    }

    @Unroll
    def "createColumnSnapshotAction parameters are correct"() {
        when:
        def db = new MockDatabase()
        db.setMaxSnapshotContainerDepth(maxDepth)
        def scope = JUnitScope.getInstance(db)

        QueryJdbcMetaDataAction action = new SnapshotColumnsLogicJdbc().createColumnSnapshotAction(name, scope)

        then:
        action.method == "getColumns"
        action.arguments == arguments

        where:
        name                                         | maxDepth | arguments
        new ObjectName("cat", "schem", "tab", "col") | 2        | ["cat", "schem", "tab", "col"]
        new ObjectName("schem", "tab", "col")        | 2        | [null, "schem", "tab", "col"]
        new ObjectName("schem", "tab", "col")        | 1        | ["schem", null, "tab", "col"]
        new ObjectName("tab", "col")                 | 2        | [null, null, "tab", "col"]
        new ObjectName("tab", "col")                 | 1        | [null, null, "tab", "col"]
        new ObjectName("tab", "col")                 | 0        | [null, null, "tab", "col"]
    }
}
