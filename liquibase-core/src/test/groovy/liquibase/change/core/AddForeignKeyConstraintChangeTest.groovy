package liquibase.change.core;

import liquibase.change.ChangeFactory;
import liquibase.change.StandardChangeTest;
import liquibase.database.Database;
import liquibase.database.core.MockDatabase;
import liquibase.database.core.SQLiteDatabase
import liquibase.snapshot.MockSnapshotGeneratorFactory
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.AddForeignKeyConstraintStatement
import liquibase.structure.core.Column
import liquibase.structure.core.ForeignKey
import liquibase.structure.core.Table
import spock.lang.Unroll;

import static org.junit.Assert.*;
import org.junit.Test;

public class AddForeignKeyConstraintChangeTest extends StandardChangeTest {

    def getConfirmationMessage() throws Exception {
        when:
        def change = new AddForeignKeyConstraintChange();
        change.setConstraintName("FK_NAME");
        change.setBaseTableSchemaName("SCHEMA_NAME");
        change.setBaseTableName("TABLE_NAME");
        change.setBaseColumnNames("COL_NAME");

        then: change.getConfirmationMessage() == "Foreign key contraint added to TABLE_NAME (COL_NAME)"
    }

    @Unroll
    def "verifyUpdate"() {
        when:
        def database = new MockDatabase()
        def snapshotFactory = new MockSnapshotGeneratorFactory()
        SnapshotGeneratorFactory.instance = snapshotFactory

        def baseTable = new Table(null, null, "base_table")
        def baseColumn = new Column(Table.class, null, null, baseTable.name, "base_col")
        baseTable.getColumns().add(new Column(Table.class, null, null, baseTable.name, "other_col"))
        baseTable.getColumns().add(baseColumn)

        def refTable = new Table(null, null, snapshotRefTable)
        def refColumn = new Column(Table.class, null, null, refTable.name, snapshotRefColumn)

        snapshotFactory.addObjects(baseTable, refColumn)

        def change = new AddForeignKeyConstraintChange()
        change.baseTableName = baseTable.name
        change.baseColumnNames = baseColumn.name
        change.referencedTableName = "ref_table"
        change.referencedColumnNames = "ref_col"
        if (changeInitiallyDeferred != null) {
            change.setInitiallyDeferred(changeInitiallyDeferred)
        }
        if (changeDeferrable != null) {
            change.setDeferrable(changeDeferrable)
        }

        then: "no table yet"
        assert !change.verifyExecuted(database).passed

        when: "Objects exist no FK"
        snapshotFactory.addObjects(baseTable, refTable)
        then:
        assert change.verifyExecuted(database).verifiedFailed

        when: "FK exists"
        def fk = new ForeignKey(null, null, null, baseTable.name, baseColumn.name).setPrimaryKeyTable(refTable).addPrimaryKeyColumn(refColumn.name)
        if (snapshotInitiallyDeferred != null) {
            fk.setInitiallyDeferred(snapshotInitiallyDeferred)
        }
        if (snapshotDeferrable != null) {
            fk.setDeferrable(snapshotDeferrable)
        }
        snapshotFactory.addObjects(fk)
        then:
        assert change.verifyExecuted(database).verifiedPassed == expectedResult

        where:
        snapshotRefTable | snapshotRefColumn | changeDeferrable | changeInitiallyDeferred | snapshotDeferrable | snapshotInitiallyDeferred | expectedResult
        "ref_table"  | "ref_col"  | null | null | null | null | true
        "ref_table2" | "ref_col"  | null | null | null | null | false
        "ref_table"  | "ref_col2" | null | null | null | null | false
        "ref_table"  | "ref_col"  | true | true | true | true | true
        "ref_table"  | "ref_col"  | null | null | null | true | true
        "ref_table"  | "ref_col"  | null | null | true | null | true
        "ref_table"  | "ref_col"  | true | null | null | null | false
        "ref_table"  | "ref_col"  | null | true | null | null | false
    }

    def "verifyNotExecuted"() {
        when:
        def database = new MockDatabase()
        def snapshotFactory = new MockSnapshotGeneratorFactory()
        SnapshotGeneratorFactory.instance = snapshotFactory

        def baseTable = new Table(null, null, "base_table")
        def baseColumn = new Column(Table.class, null, null, baseTable.name, "base_col")
        baseTable.getColumns().add(new Column(Table.class, null, null, baseTable.name, "other_col"))
        baseTable.getColumns().add(baseColumn)

        def refTable = new Table(null, null, "ref_table")
        def refColumn = new Column(Table.class, null, null, refTable.name, "ref_col")
        refTable.getColumns().add(refColumn)

        snapshotFactory.addObjects(baseTable, refTable)

        def change = new AddForeignKeyConstraintChange()
        change.baseTableName = baseTable.name
        change.baseColumnNames = baseColumn.name
        change.referencedTableName = refTable.name
        change.referencedColumnNames = refColumn.name

        then: "fk is not there"
        assert change.verifyNotExecuted(database).verifiedPassed

        when: "FK is there"
        snapshotFactory.addObjects(new ForeignKey(null, null, null, baseTable.name, baseColumn.name).setPrimaryKeyTable(refTable).addPrimaryKeyColumn(refColumn.name))
        then:
        assert change.verifyNotExecuted(database).verifiedFailed
    }
}
