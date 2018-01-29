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

class DropAllForeignKeyConstraintsChangeTest extends Specification {

    def "basic properties"() {
        when:
        def change = new DropAllForeignKeyConstraintsChange()
        change.setBaseTableCatalogName("LBCAT2")
        change.setBaseTableSchemaName("LBSCHEM2")
        change.setBaseTableName("EXAMPLE")

        then:
        change.getBaseTableCatalogName() == "LBCAT2"
        change.getBaseTableSchemaName() == "LBSCHEM2"
        change.getBaseTableName() == "EXAMPLE"
        change.getSerializedObjectNamespace() == "http://www.liquibase.org/xml/ns/dbchangelog"
    }

    @Unroll("SQL in #shortDbName for dropAllForeignKeys")
    def "GenerateStatements"() {
        when:
        Database dbms = DatabaseFactory.getInstance().getDatabase(shortDbName)
        OfflineConnection conn = new OfflineConnection("offline:" + shortDbName, null)
        dbms.setConnection(conn)

        def snapshotFactory = new MockSnapshotGeneratorFactory()
        SnapshotGeneratorFactory.instance = snapshotFactory

        def catalogBase = new Catalog("cat_base")
        def catalogRef = new Catalog("cat_ref")
        def schemaBase = new Schema(catalogBase, "schema_base")
        def schemaRef = new Schema(catalogRef, "schemaRef")
        snapshotFactory.addObjects(catalogBase, catalogRef, schemaBase, schemaRef)

        def baseTable = new Table("cat_base", "schema_base", "base_table")
        def baseColumn1 = new Column(Table.class, "cat_base", "schema_base",
                baseTable.name, "base_col1")
        baseTable.addColumn(baseColumn1)
        def baseColumn2 = new Column(Table.class, "cat_base", "schema_base",
                baseTable.name, "base_col2")
        baseTable.addColumn(baseColumn2)
        snapshotFactory.addObjects(baseTable)

        def refTable = new Table("cat_ref", "schema_ref", "ref_table")
        def refColumn = new Column(Table.class, "cat_ref", "schema_ref",
                refTable.name, "ref_col")

        def refPk = new PrimaryKey().setName("pk_ref").setTable(refTable)
        refPk.addColumn(0, refColumn)
        refTable.setPrimaryKey(refPk)
        snapshotFactory.addObjects(refTable)

        def fk1 = new ForeignKey("fk_base_ref1", "cat_base", "schema_base",
                baseTable.name, new Column(baseColumn1.name))
                .setPrimaryKeyTable(refTable)
                .addPrimaryKeyColumn(new Column(refColumn.name))
        snapshotFactory.addObjects(fk1)

        def fk2 = new ForeignKey("fk_base_ref2", "cat_base", "schema_base",
                baseTable.name, new Column(baseColumn2.name))
                .setPrimaryKeyTable(refTable)
                .addPrimaryKeyColumn(new Column(refColumn.name))
        snapshotFactory.addObjects(fk2)
        def fkList = [fk1, fk2]
        baseTable.setAttribute("outgoingForeignKeys", fkList)

        DatabaseObject[] catalogs = [catalogBase, catalogRef]

        DatabaseSnapshot snap = snapshotFactory.createSnapshot(
                catalogs, dbms, new SnapshotControl(dbms, (String) null))
        conn.setSnapshot(snap)

        then:
        "Virtual state of database initialized"

        when:
        def change = new DropAllForeignKeyConstraintsChange()
        change.setBaseTableCatalogName("cat_base")
        change.setBaseTableSchemaName("schema_base")
        change.setBaseTableName("base_table")
        Sql[] sqls
        sqls = SqlGeneratorFactory.getInstance().generateSql(change, dbms)

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
        "db2"        | ["ALTER TABLE schema_base.base_table DROP CONSTRAINT fk_base_ref1",
                        "ALTER TABLE schema_base.base_table DROP CONSTRAINT fk_base_ref2"]
        "derby"      | ["ALTER TABLE schema_base.base_table DROP CONSTRAINT fk_base_ref1",
                        "ALTER TABLE schema_base.base_table DROP CONSTRAINT fk_base_ref2"]
        "firebird"   | ["ALTER TABLE base_table DROP CONSTRAINT fk_base_ref1",
                        "ALTER TABLE base_table DROP CONSTRAINT fk_base_ref2"]
        "h2"         | ["ALTER TABLE schema_base.base_table DROP CONSTRAINT fk_base_ref1",
                        "ALTER TABLE schema_base.base_table DROP CONSTRAINT fk_base_ref2"]
        "hsqldb"     | ["ALTER TABLE schema_base.base_table DROP CONSTRAINT fk_base_ref1",
                        "ALTER TABLE schema_base.base_table DROP CONSTRAINT fk_base_ref2"]
        "informix"   | ["ALTER TABLE schema_base.base_table DROP CONSTRAINT fk_base_ref1",
                        "ALTER TABLE schema_base.base_table DROP CONSTRAINT fk_base_ref2"]
        "mariadb"    | ["ALTER TABLE schema_base.base_table DROP FOREIGN KEY fk_base_ref1",
                        "ALTER TABLE schema_base.base_table DROP FOREIGN KEY fk_base_ref2"]
        "mysql"      | ["ALTER TABLE schema_base.base_table DROP FOREIGN KEY fk_base_ref1",
                        "ALTER TABLE schema_base.base_table DROP FOREIGN KEY fk_base_ref2"]
        "oracle"     | ["ALTER TABLE schema_base.base_table DROP CONSTRAINT fk_base_ref1",
                        "ALTER TABLE schema_base.base_table DROP CONSTRAINT fk_base_ref2"]
        "postgresql" | ["ALTER TABLE schema_base.base_table DROP CONSTRAINT fk_base_ref1",
                        "ALTER TABLE schema_base.base_table DROP CONSTRAINT fk_base_ref2"]
        "asany"      | ["ALTER TABLE schema_base.base_table DROP FOREIGN KEY fk_base_ref1",
                        "ALTER TABLE schema_base.base_table DROP FOREIGN KEY fk_base_ref2"]
        "sybase"     | ["ALTER TABLE [schema_base].[base_table] DROP CONSTRAINT [fk_base_ref1]",
                        "ALTER TABLE [schema_base].[base_table] DROP CONSTRAINT [fk_base_ref2]"]


    }

    def "GetConfirmationMessage"() {
        when:
        def change = new DropAllForeignKeyConstraintsChange()
        change.setBaseTableName("EXAMPLE")

        then:
        change.getConfirmationMessage() == "Foreign keys on base table " + change.getBaseTableName() + " dropped"

    }

    def "GenerateStatementsVolatile"() {
    }

}
