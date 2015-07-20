package liquibase.actionlogic.core

import liquibase.JUnitScope
import liquibase.action.core.QueryJdbcMetaDataAction
import liquibase.action.core.SnapshotDatabaseObjectsAction
import liquibase.actionlogic.RowBasedQueryResult
import liquibase.exception.ActionPerformException
import liquibase.sdk.database.MockDatabase
import liquibase.structure.ObjectName
import liquibase.structure.ObjectReference
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
        ]), new SnapshotDatabaseObjectsAction(Column, new ObjectReference(Table.class)), scope)

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

    @Unroll
    def "createColumnSnapshotAction parameters are correct"() {
        when:
        def db = new MockDatabase()
        db.setSupportsCatalogs(supportsCatalogs)
        db.setSupportsSchemas(supportsSchemas)
        def scope = JUnitScope.getInstance(db)

        QueryJdbcMetaDataAction action = new SnapshotColumnsLogicJdbc().createColumnSnapshotAction(name, scope)

        then:
        action.method == "getColumns"
        action.arguments == arguments

        where:
        name                                         | supportsCatalogs | supportsSchemas | arguments
        new ObjectName("cat", "schem", "tab", "col") | true             | true            | ["cat", "schem", "tab", "col"]
        new ObjectName("schem", "tab", "col")        | true             | true            | [null, "schem", "tab", "col"]
        new ObjectName("schem", "tab", "col")        | false            | true            | ["schem", null, "tab", "col"]
        new ObjectName("tab", "col")                 | true             | true            | [null, null, "tab", "col"]
        new ObjectName("tab", "col")                 | false            | true            | [null, null, "tab", "col"]
        new ObjectName("tab", "col")                 | false            | false           | [null, null, "tab", "col"]
    }
}
