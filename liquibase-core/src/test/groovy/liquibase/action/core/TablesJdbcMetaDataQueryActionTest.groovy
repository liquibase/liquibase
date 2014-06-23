package liquibase.action.core

import liquibase.RuntimeEnvironment
import liquibase.database.jvm.JdbcConnection
import liquibase.executor.ExecutionOptions
import liquibase.sdk.database.MockDatabase
import liquibase.sdk.database.MockResultSet
import liquibase.structure.core.Table
import spock.lang.Specification
import spock.lang.Unroll

import java.sql.DatabaseMetaData

class TablesJdbcMetaDataQueryActionTest extends Specification {

    @Unroll
    def "getRawMetaData"() {
        when:
        def action = new TablesJdbcMetaDataQueryAction(catalogName, schemaName, tableName)
        def database = new MockDatabase()
        def connection = Mock(JdbcConnection)
        def metaData = Mock(DatabaseMetaData)

        connection.getMetaData() >> metaData
        1 * metaData.getTables(catalogName, schemaName, tableName, ["TABLE"].toArray()) >> new MockResultSet()
        database.setConnection(connection)

        then:
        action.getRawMetaData(new ExecutionOptions(new RuntimeEnvironment(database)))

        where:
        catalogName | schemaName    | tableName
        "cat_name"  | "schema_name" | "table_name"
        "cat_name"  | "schema_name" | null
    }

    def "rawMetaDataToObject from mysql"() {
        when:
        def action = new TablesJdbcMetaDataQueryAction(null, null, null)
        def Table table = action.rawMetaDataToObject([
                REF_GENERATION           : null,
                TYPE_NAME                : null,
                TABLE_NAME               : "cart_item",
                TYPE_CAT                 : null,
                REMARKS                  : "",
                TYPE_SCHEM               : null,
                TABLE_TYPE               : "TABLE",
                TABLE_SCHEM              : null,
                TABLE_CAT                : "lbcat",
                SELF_REFERENCING_COL_NAME: null,
        ], options)

        then:
        table.getName() == "cart_item"
        table.getSchema().getCatalogName() == "lbcat"
        table.getRemarks() == null
    }

    def "rawMetaDataToObject with remarks from mysql"() {
        when:
        def action = new TablesJdbcMetaDataQueryAction(null, null, null)
        def Table table = action.rawMetaDataToObject([
                REF_GENERATION           : null,
                TYPE_NAME                : null,
                TABLE_NAME               : "cart_item",
                TYPE_CAT                 : null,
                REMARKS                  : "here are my remarks",
                TYPE_SCHEM               : null,
                TABLE_TYPE               : "TABLE",
                TABLE_SCHEM              : null,
                TABLE_CAT                : "lbcat",
                SELF_REFERENCING_COL_NAME: null,
        ], options)

        then:
        table.getName() == "cart_item"
        table.getSchema().getCatalogName() == "lbcat"
        table.getRemarks() == "here are my remarks"
    }
}
