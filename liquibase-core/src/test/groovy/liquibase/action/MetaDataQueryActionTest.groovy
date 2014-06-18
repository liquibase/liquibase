package liquibase.action

import liquibase.action.metadata.FetchTablesAction
import spock.lang.Specification
import spock.lang.Unroll

class MetaDataQueryActionTest extends Specification {

    @Unroll
    def "merge that is valid"() {
        expect:
        new MockMetaDataAction(action1).merge(new MockMetaDataAction(action2)).toString() == expected

        where:
        action1                                                                              | action2                                                                            | expected
        [tableName: "table_name", schemaName: "schema_name", catalogName: "catalog_name"]    | [tableName: "table_name", schemaName: "schema_name", catalogName: "catalog_name"]  | "MockMetaDataAction(catalogName=catalog_name, schemaName=schema_name, tableName=table_name)"
        [tableName: "table_name", schemaName: "schema_name", catalogName: "catalog_name"]    | [tableName: "table_name2", schemaName: "schema_name", catalogName: "catalog_name"] | "MockMetaDataAction(catalogName=catalog_name, schemaName=schema_name)"
        [tableName: "table_name2", schemaName: "schema_name", catalogName: "catalog_name"]   | [tableName: "table_name", schemaName: "schema_name", catalogName: "catalog_name"]  | "MockMetaDataAction(catalogName=catalog_name, schemaName=schema_name)"
        [tableName: "table_name", schemaName: "schema_name", catalogName: "catalog_name"]    | [tableName: "table_name", schemaName: "schema_name2", catalogName: "catalog_name"] | "MockMetaDataAction(catalogName=catalog_name, tableName=table_name)"
        [tableName: "table_name", schemaName: "schema_name", catalogName: "catalog_name"]    | [tableName: "table_name", schemaName: "schema_name", catalogName: "catalog_name2"] | "MockMetaDataAction(schemaName=schema_name, tableName=table_name)"
        [tableName: "table_name", schemaName: "schema_name", catalogName: "catalog_name"]    | [schemaName: "schema_name", catalogName: "catalog_name"]                           | "MockMetaDataAction(catalogName=catalog_name, schemaName=schema_name)"
        [schemaName: "schema_name", catalogName: "catalog_name"]                             | [tableName: "table_name", schemaName: "schema_name", catalogName: "catalog_name"]  | "MockMetaDataAction(catalogName=catalog_name, schemaName=schema_name)"
        [tableName: "table_name", catalogName: "catalog_name"]                               | [tableName: "table_name", schemaName: "schema_name"]                               | "MockMetaDataAction(tableName=table_name)"
        [tableName: "table_name2", schemaName: "schema_name2", catalogName: "catalog_name2"] | [tableName: "table_name", schemaName: "schema_name", catalogName: "catalog_name"]  | "MockMetaDataAction()"
    }

    def "merge that is not valid"() {
        expect:
        new MockMetaDataAction([tableName: "table_name"]).merge(new FetchTablesAction(null, null, "table_name")) == null
    }
}
