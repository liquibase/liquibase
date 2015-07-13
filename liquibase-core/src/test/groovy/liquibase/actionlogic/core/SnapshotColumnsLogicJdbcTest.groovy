package liquibase.actionlogic.core

import liquibase.JUnitScope
import liquibase.action.core.SnapshotDatabaseObjectsAction
import liquibase.actionlogic.RowBasedQueryResult
import liquibase.exception.ActionPerformException
import liquibase.sdk.database.MockDatabase
import liquibase.structure.ObjectName
import liquibase.structure.core.Column
import liquibase.structure.core.Table
import spock.lang.Specification
import spock.lang.Unroll

import java.sql.DatabaseMetaData
import java.sql.Types

class SnapshotColumnsLogicJdbcTest extends Specification {

    @Unroll
    def "cannot snapshot using a table with a name longer than the database supports"() {
        when:
        def database = new MockDatabase()
        database.setMaxContainerDepth(maxDepth)
        def scope = JUnitScope.getInstance(database)
        new SnapshotColumnsLogicJdbc().execute(new SnapshotDatabaseObjectsAction(Column, object), scope)

        then:
        def e = thrown(ActionPerformException)
        e.message == message

        where:
        object                                                     | message                                                        | maxDepth
        new Table("catName", "schemaName", "tableName")            | "Cannot snapshot a table with 2 level(s) of hierarchy on mock" | 1
        new Table("catName", "schemaName", "tableName")            | "Cannot snapshot a table with 2 level(s) of hierarchy on mock" | 0
        new Table(new ObjectName("schemaName", "tableName"))       | "Cannot snapshot a table with 1 level(s) of hierarchy on mock" | 0
        new Table(new ObjectName(null, "schemaName", "tableName")) | "Cannot snapshot a table with 1 level(s) of hierarchy on mock" | 0
    }

    @Unroll
    def "convertObject handles column name and table plus container correctly"() {
        when:
        def database = new MockDatabase();
        database.setMaxContainerDepth(maxDepth)
        def scope = JUnitScope.getInstance(database)

        def object = new SnapshotColumnsLogicJdbc().convertToObject(new RowBasedQueryResult.Row([
                TABLE_CAT  : tableCat,
                TABLE_SCHEM: tableSchema,
                TABLE_NAME : tableName,
                COLUMN_NAME: "columnName",
                NULLABLE   : DatabaseMetaData.columnNoNulls,
                DATA_TYPE  : Types.INTEGER,
        ]), new SnapshotDatabaseObjectsAction(Column, new Table()), scope)

        then:
        object instanceof Column
        object.name.toString() == "columnName"
        object.relation.name.toString() == expected

        where:
        tableCat   | tableSchema  | tableName   | maxDepth | expected
        "tableCat" | "schemaName" | "tableName" | 2        | "tableCat.schemaName.tableName"
        null       | "schemaName" | "tableName" | 2        | "#DEFAULT.schemaName.tableName"
        "tableCat" | null         | "tableName" | 2        | "tableCat.#DEFAULT.tableName"
        "tableCat" | "schemaName" | "tableName" | 1        | "schemaName.tableName"
        "tableCat" | null         | "tableName" | 1        | "tableCat.tableName"
        null       | "schemaName" | "tableName" | 1        | "schemaName.tableName"
        null       | null         | "tableName" | 0        | "tableName"
    }
}
