package liquibase.snapshot

import liquibase.action.MockMetaDataAction
import liquibase.action.QueryAction
import liquibase.action.core.ColumnsJdbcMetaDataQueryAction
import liquibase.action.core.TablesJdbcMetaDataQueryAction
import spock.lang.Specification

class SnapshotGeneratorFactoryTest extends Specification {

    def "mergeActions with a single type"() {
        when:
        def original = [
                new MockMetaDataAction([tableName: "table_name1", schemaName: "schema_name"]),
                new MockMetaDataAction([tableName: "table_name2", schemaName: "schema_name"]),
                new MockMetaDataAction([tableName: "table_name3", schemaName: "schema_name"]),
        ]
        def expected = [new MockMetaDataAction([schemaName: "schema_name"])]

        then:
        SnapshotGeneratorFactory.instance.mergeActions(original) == expected
    }

    def "mergeActions with multiple types"() {
        when:
        def original = [
                new MockMetaDataAction([tableName: "table_name1", schemaName: "schema_name"]),
                new MockMetaDataAction([tableName: "table_name2", schemaName: "schema_name"]),
                new MockMetaDataAction([tableName: "table_name3", schemaName: "schema_name"]),
                new TablesJdbcMetaDataQueryAction(null, "schema_name", "table_1"),
                new TablesJdbcMetaDataQueryAction(null, "schema_name", "table_2"),
                new TablesJdbcMetaDataQueryAction(null, "schema_name", "table_3"),
                new ColumnsJdbcMetaDataQueryAction(null, "schema_name", "table_1", "column_1"),
                new ColumnsJdbcMetaDataQueryAction(null, "schema_name", "table_1", "column_2"),
                new ColumnsJdbcMetaDataQueryAction(null, "schema_name", "table_1", "column_3"),
        ]
        def expected = [
                new MockMetaDataAction([schemaName: "schema_name"]),
                new TablesJdbcMetaDataQueryAction(null, "schema_name", null),
                new ColumnsJdbcMetaDataQueryAction(null, "schema_name", "table_1", null),
        ]

        then:
        SnapshotGeneratorFactory.instance.mergeActions(original) == expected
    }

    def "mergeActions with types that don't merge"() {
        when:
        def original = [
                new NonMergingAction([tableName: "table_nameX", schemaName: "schema_name"]),
                new MockMetaDataAction([tableName: "table_name1", schemaName: "schema_name"]),
                new NonMergingAction([tableName: "table_nameY", schemaName: "schema_name"]),
                new MockMetaDataAction([tableName: "table_name2", schemaName: "schema_name"]),
                new MockMetaDataAction([tableName: "table_name3", schemaName: "schema_name"]),
                new NonMergingAction([tableName: "table_nameZ", schemaName: "schema_name"]),
                new TablesJdbcMetaDataQueryAction(null, "schema_name", "table_2"),
                new TablesJdbcMetaDataQueryAction(null, "schema_name", "table_3"),
                new ColumnsJdbcMetaDataQueryAction(null, "schema_name", "table_1", "column_1"),
                new NonMergingAction([tableName: "table_nameA", schemaName: "schema_name"]),
                new ColumnsJdbcMetaDataQueryAction(null, "schema_name", "table_1", "column_2"),
                new ColumnsJdbcMetaDataQueryAction(null, "schema_name", "table_1", "column_3"),
        ]
        def expected = [
                new NonMergingAction([tableName: "table_nameX", schemaName: "schema_name"]),
                new MockMetaDataAction([schemaName: "schema_name"]),
                new NonMergingAction([tableName: "table_nameY", schemaName: "schema_name"]),
                new NonMergingAction([tableName: "table_nameZ", schemaName: "schema_name"]),
                new TablesJdbcMetaDataQueryAction(null, "schema_name", null),
                new ColumnsJdbcMetaDataQueryAction(null, "schema_name", "table_1", null),
                new NonMergingAction([tableName: "table_nameA", schemaName: "schema_name"]),
        ]

        then:
        SnapshotGeneratorFactory.instance.mergeActions(original) == expected
    }

    private static class NonMergingAction extends MockMetaDataAction {
        NonMergingAction(Map<String, Object> attributes) {
            super(attributes)
        }

        @Override
        boolean merge(QueryAction action) {
            return false;
        }
    }
}
