package liquibase.change.core;

import liquibase.change.ChangeFactory;
import liquibase.change.StandardChangeTest;
import liquibase.database.Database;
import liquibase.database.core.FirebirdDatabase;
import liquibase.database.core.HsqlDatabase
import liquibase.database.core.MockDatabase;
import liquibase.database.core.SQLiteDatabase
import liquibase.snapshot.MockSnapshotGeneratorFactory
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.SqlStatement
import liquibase.structure.core.Column
import liquibase.structure.core.ForeignKey
import liquibase.structure.core.Table;

import static org.junit.Assert.*;

public class AddLookupTableChangeTest extends StandardChangeTest {

    def getConfirmationMessage() throws Exception {
        when:
        def change = new AddLookupTableChange();
        change.setExistingTableName("OLD_TABLE_NAME");
        change.setExistingColumnName("OLD_COLUMN_NAME");

        then:
        change.getConfirmationMessage() == "Lookup table added for OLD_TABLE_NAME.OLD_COLUMN_NAME"
    }

    def "verifyExecuted and verifyNotExecuted"() {
        when:
        def database = new MockDatabase()
        def snapshotFactory = new MockSnapshotGeneratorFactory()
        SnapshotGeneratorFactory.instance = snapshotFactory

        def baseTable = new Table(null, null, "base_table")
        def baseColumn = new Column(Table.class, null, null, baseTable.name, "base_col")
        baseTable.getColumns().add(new Column(Table.class, null, null, baseTable.name, "other_col"))
        baseTable.getColumns().add(baseColumn)

        def newTable = new Table(null, null, "new_table")
        def newColumn = new Column(Table.class, null, null, newTable.name, "new_col")
        newTable.getColumns().add(newColumn)

        def fk = new ForeignKey("fk_test", null, null, baseTable.name, baseColumn.name).setPrimaryKeyTable(newTable).addPrimaryKeyColumn(newColumn.name)

        snapshotFactory.addObjects(baseTable)

        def change = new AddLookupTableChange()
        change.existingTableName = baseTable.name
        change.existingColumnName = baseColumn.name
        change.newTableName = newTable.name
        change.newColumnName = newColumn.name
        change.setConstraintName(fk.name)

        then: "no new table yet"
        assert change.verifyExecuted(database).verifiedFailed
        assert change.verifyNotExecuted(database).verifiedPassed

        when: "new table exists but no FK"
        snapshotFactory.addObjects(newTable)
        then:
        assert change.verifyExecuted(database).verifiedFailed
        assert change.verifyNotExecuted(database).verifiedFailed

        when: "FK and table exist"
        snapshotFactory.addObjects(fk)
        then:
        assert change.verifyExecuted(database).verifiedPassed
        assert change.verifyNotExecuted(database).verifiedFailed
    }
}
