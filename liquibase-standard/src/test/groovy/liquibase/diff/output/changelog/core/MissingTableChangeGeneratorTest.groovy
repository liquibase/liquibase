package liquibase.diff.output.changelog.core

import liquibase.change.core.CreateTableChange
import liquibase.database.core.MockDatabase
import liquibase.database.core.PostgresDatabase
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

    def "fixMissing propagates partitionBy from Postgres reference Table onto CreateTableChange"() {
        when:
        def generator = new MissingTableChangeGenerator()
        def control = new DiffOutputControl(false, false, false, null)

        def table = new Table(null, "public", "test_tbl_part")
        table.addColumn(new Column("test_date_int").setType(new DataType("int")))
        table.setPartitionBy("RANGE (test_date_int)")

        def referenceDatabase = new PostgresDatabase()
        def comparisonDatabase = new PostgresDatabase()

        def changes = generator.fixMissing(table, control, referenceDatabase, comparisonDatabase, new ChangeGeneratorChain(null))

        then:
        changes.length == 1
        ((CreateTableChange) changes[0]).getPartitionBy() == "RANGE (test_date_int)"
    }

    def "fixMissing leaves partitionBy null when reference Table has no partition spec"() {
        when:
        def generator = new MissingTableChangeGenerator()
        def control = new DiffOutputControl(false, false, false, null)

        def table = new Table(null, "public", "plain_tbl")
        table.addColumn(new Column("id").setType(new DataType("int")))
        // no setPartitionBy(...) call

        def changes = generator.fixMissing(table, control, new PostgresDatabase(), new PostgresDatabase(), new ChangeGeneratorChain(null))

        then:
        ((CreateTableChange) changes[0]).getPartitionBy() == null
    }
}
