package liquibase.actionlogic.core

import liquibase.JUnitScope
import liquibase.action.core.QueryJdbcMetaDataAction
import liquibase.action.core.SnapshotDatabaseObjectsAction
import liquibase.actionlogic.RowBasedQueryResult
import liquibase.exception.ActionPerformException
import liquibase.sdk.database.MockDatabase
import liquibase.structure.ObjectName
import liquibase.structure.ObjectReference
import liquibase.structure.core.Catalog
import liquibase.structure.core.Column
import liquibase.structure.core.Schema
import liquibase.structure.core.Table
import spock.lang.Specification
import spock.lang.Unroll

class SnapshotTablesLogicJdbcTest extends Specification {

    @Unroll
    def "convertObject handles column name and table plus container correctly"() {
        when:
        def database = new MockDatabase();
        database.setMaxReferenceContainerDepth(maxDepth)
        def scope = JUnitScope.getInstance(database)

        def object = new SnapshotTablesLogicJdbc().convertToObject(new RowBasedQueryResult.Row([
                TABLE_CAT  : tableCat,
                TABLE_SCHEM: tableSchema,
                TABLE_NAME : tableName,
        ]), new SnapshotDatabaseObjectsAction(Column, new ObjectReference(Table)), scope)

        then:
        object instanceof Table
        object.name.toString() == expected

        where:
        tableCat   | tableSchema  | tableName   | maxDepth | expected
        "tableCat" | "schemaName" | "tableName" | 2        | "tableCat.schemaName.tableName"
        null       | "schemaName" | "tableName" | 2        | "schemaName.tableName"
        "tableCat" | null         | "tableName" | 2        | "tableCat.#UNSET.tableName"
        "tableCat" | "schemaName" | "tableName" | 1        | "schemaName.tableName"
        "tableCat" | null         | "tableName" | 1        | "tableCat.tableName"
        null       | "schemaName" | "tableName" | 1        | "schemaName.tableName"
        null       | null         | "tableName" | 0        | "tableName"
    }

    @Unroll
    def "createSnapshotAction parameters are correct"() {
        when:
        def db = new MockDatabase()
        db.setMaxSnapshotContainerDepth(maxDepth)
        def scope = JUnitScope.getInstance(db)

        def action = new SnapshotDatabaseObjectsAction(Table, new ObjectReference(relatedType, name))

        QueryJdbcMetaDataAction queryAction = new SnapshotTablesLogicJdbc().createSnapshotAction(action, scope)

        then:
        queryAction.method == "getTables"
        queryAction.arguments == arguments

        where:
        relatedType | name                                  | maxDepth | arguments
        Table       | new ObjectName("cat", "schem", "tab") | 2        | ["cat", "schem", "tab", ["TABLE"]]
        Table       | new ObjectName("schem", "tab")        | 2        | [null, "schem", "tab", ["TABLE"]]
        Table       | new ObjectName("schem", "tab")        | 1        | ["schem", null, "tab", ["TABLE"]]
        Table       | new ObjectName("tab")                 | 2        | [null, null, "tab", ["TABLE"]]
        Table       | new ObjectName("tab")                 | 1        | [null, null, "tab", ["TABLE"]]
        Table       | new ObjectName("tab")                 | 0        | [null, null, "tab", ["TABLE"]]
        Schema      | new ObjectName("cat", "schema")       | 2        | ["cat", "schema", null, ["TABLE"]]
        Schema      | new ObjectName("schema")              | 2        | [null, "schema", null, ["TABLE"]]
        Schema      | new ObjectName("schema")              | 1        | ["schema", null, null, ["TABLE"]]
        Catalog     | new ObjectName("cat")                 | 2        | ["cat", null, null, ["TABLE"]]
    }
}
