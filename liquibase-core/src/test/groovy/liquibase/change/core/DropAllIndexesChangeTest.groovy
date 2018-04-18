package liquibase.change.core

import liquibase.database.Database
import liquibase.database.DatabaseFactory
import liquibase.database.OfflineConnection
import liquibase.snapshot.DatabaseSnapshot
import liquibase.snapshot.MockSnapshotGeneratorFactory
import liquibase.snapshot.SnapshotControl
import liquibase.snapshot.SnapshotGeneratorFactory
import liquibase.sql.Sql
import liquibase.sqlgenerator.SqlGeneratorFactory
import liquibase.structure.DatabaseObject
import liquibase.structure.core.*
import spock.lang.Specification
import spock.lang.Unroll

class DropAllIndexesChangeTest extends Specification {

    def "basic properties"() {
        when:
        def change = new DropAllIndexesChange()
        change.setCatalogName("LBCAT2")
        change.setSchemaName("LBSCHEM2")
        change.setTableName("EXAMPLE")

        then:
        change.getCatalogName() == "LBCAT2"
        change.getSchemaName() == "LBSCHEM2"
        change.getTableName() == "EXAMPLE"
        change.getSerializedObjectNamespace() == "http://www.liquibase.org/xml/ns/dbchangelog"
    }

    @Unroll("SQL in #shortDbName for dropAllIndexes")
    def "GenerateStatements"() {
        when:
        Database dbms = DatabaseFactory.getInstance().getDatabase(shortDbName)
        OfflineConnection conn = new OfflineConnection("offline:" + shortDbName, null)
        dbms.setConnection(conn)

        def snapshotFactory = new MockSnapshotGeneratorFactory()
        SnapshotGeneratorFactory.instance = snapshotFactory

        def catalog = new Catalog("cat_base")
        def schema = new Schema(catalog, "schema_base")
        snapshotFactory.addObjects(catalog, schema)

        def table = new Table(catalog.name, schema.name, "base_table")
        def column1 = new Column(Table.class, catalog.name, schema.name, table.name, "base_col1")
        table.addColumn(column1)
        def column2 = new Column(Table.class, catalog.name, schema.name, table.name, "base_col2")
        table.addColumn(column2)
        snapshotFactory.addObjects(table)

        def index1 = new Index("idx_test1", catalog.name, schema.name, table.name, new Column(column1.name))
        def index2 = new Index("idx_test2", catalog.name, schema.name, table.name, new Column(column2.name))
        snapshotFactory.addObjects(index1, index2)

        DatabaseObject[] catalogs = [catalog]

        DatabaseSnapshot snap = snapshotFactory.createSnapshot(
                catalogs, dbms, new SnapshotControl(dbms, (String) null))
        conn.setSnapshot(snap)

        then:
        "Virtual state of database initialized"

        when:
        def change = new DropAllIndexesChange()
        change.setCatalogName("cat_base")
        change.setSchemaName("schema_base")
        change.setTableName("base_table")
        Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(change, dbms)

        then:
        for (int i = 0; i < sqls.length; i++) {
            String sql = sqls[i].toSql()
            String expectation = expectedValue[i]
            assert (sql.equalsIgnoreCase(expectation))
        }

        cleanup:
        DatabaseFactory.reset()

        where:
        shortDbName  | expectedValue
        "db2"        | ["DROP INDEX idx_test1",
                        "DROP INDEX idx_test2"]
        "derby"      | ["DROP INDEX idx_test1",
                        "DROP INDEX idx_test2"]
        "firebird"   | ["DROP INDEX idx_test1",
                        "DROP INDEX idx_test2"]
        "h2"         | ["DROP INDEX idx_test1",
                        "DROP INDEX idx_test2"]
        "hsqldb"     | ["DROP INDEX idx_test1",
                        "DROP INDEX idx_test2"]
        "informix"   | ["DROP INDEX idx_test1",
                        "DROP INDEX idx_test2"]
        "mariadb"    | ["DROP INDEX idx_test1 ON schema_base.base_table",
                        "DROP INDEX idx_test2 ON schema_base.base_table"]
        "mssql"      | ["DROP INDEX idx_test1",
                        "DROP INDEX idx_test2"]
        "mysql"      | ["DROP INDEX idx_test1 ON schema_base.base_table",
                        "DROP INDEX idx_test2 ON schema_base.base_table"]
        "oracle"     | ["DROP INDEX schema_base.idx_test1",
                        "DROP INDEX schema_base.idx_test2"]
        "postgresql" | ["DROP INDEX idx_test1",
                        "DROP INDEX idx_test2"]
        "sqlite"     | ["DROP INDEX schema_base.idx_test1",
                        "DROP INDEX schema_base.idx_test2"]
        "asany"      | ["DROP INDEX base_table.idx_test1",
                        "DROP INDEX base_table.idx_test2"]
        "sybase"     | ["DROP INDEX base_table.idx_test1",
                        "DROP INDEX base_table.idx_test2"]
    }

    def "GetConfirmationMessage"() {
        when:
        def change = new DropAllIndexesChange()
        change.setTableName("EXAMPLE")

        then:
        change.getConfirmationMessage() == "Indexes on table " + change.getTableName() + " dropped"

    }

    def "GenerateStatementsVolatile"() {
    }
}
