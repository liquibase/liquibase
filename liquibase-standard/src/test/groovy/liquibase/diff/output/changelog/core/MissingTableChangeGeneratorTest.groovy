package liquibase.diff.output.changelog.core

import liquibase.change.core.CreateTableChange
import liquibase.database.core.MockDatabase
import liquibase.diff.output.DiffOutputControl
import liquibase.diff.output.changelog.ChangeGeneratorChain
import liquibase.structure.core.Column
import liquibase.structure.core.DataType
import liquibase.structure.core.Table
import spock.lang.Specification
import spock.lang.Unroll

class MissingTableChangeGeneratorTest extends Specification {

    @Unroll
    def "fixMissing respects control settings"() {
        when:
        def generator = new MissingTableChangeGenerator()

        def control = new DiffOutputControl(includeCatalog, includeSchema, includeTablespace, null)

        def table = new Table("my_catalog", "my_schema", "test_table")
        table.setName("test_table")
        table.setTablespace("my_tablespace")
        table.addColumn(new Column("id").setType(new DataType("int")))

        def database = new MockDatabase() {
            @Override
            boolean supportsTablespaces() {
                return true;
            }
        }
        database.setSupportsCatalogs(true)
        database.setSupportsSchemas(true)

        def changes = generator.fixMissing(table, control, database, database, new ChangeGeneratorChain(null))

        then:
        changes.length == 1
        ((CreateTableChange) changes[0]).getTableName() == "test_table"
        ((CreateTableChange) changes[0]).getSchemaName() == expectedSchema
        ((CreateTableChange) changes[0]).getCatalogName() == expectedCatalog
        ((CreateTableChange) changes[0]).getTablespace() == expectedTablespace

        where:
        includeCatalog | includeSchema | includeTablespace | expectedCatalog | expectedSchema | expectedTablespace
        false          | false         | false             | null            | null           | null
        true           | true          | true              | "my_catalog"    | "my_schema"    | "my_tablespace"
    }
}
