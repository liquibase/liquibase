package liquibase.diff.output.changelog;

import liquibase.change.Change;
import liquibase.change.core.AddColumnChange;
import liquibase.change.core.AddForeignKeyConstraintChange;
import liquibase.changelog.ChangeSet;
import liquibase.database.MockDatabaseConnection;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.MySQLDatabase;
import liquibase.diff.DiffResult;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.DiffOutputControl;
import liquibase.snapshot.EmptyDatabaseSnapshot;
import liquibase.snapshot.SnapshotControl;
import liquibase.snapshot.SnapshotIdService;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.*;
import liquibase.util.DependencyUtil;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class DiffToChangeLogTest {

    @Test
    @SuppressWarnings("unchecked")
    public void getOrderedOutputTypes_isConsistent() throws Exception {
        MySQLDatabase database = new MySQLDatabase();
        DiffToChangeLog obj = new DiffToChangeLog(new DiffResult(new EmptyDatabaseSnapshot(database), new EmptyDatabaseSnapshot(database), new CompareControl()), null);

        for (Class<? extends ChangeGenerator> type : new Class[] {UnexpectedObjectChangeGenerator.class, MissingObjectChangeGenerator.class, ChangedObjectChangeGenerator.class}) {
            List<Class<? extends DatabaseObject>> orderedOutputTypes = obj.getOrderedOutputTypes(type);
            for (int i=0; i<50; i++) {
                assertThat("Error checking "+type.getName(), orderedOutputTypes, contains(obj.getOrderedOutputTypes(type).toArray()));
            }
        }
    }

    @Test
    public void getOrderedOutputTypes_hasDependencies() throws Exception {
        MySQLDatabase database = new MySQLDatabase();
        // note: MySQL does not support schemas, so Schema won't be included
        SnapshotControl control = new SnapshotControl(database, Schema.class, Catalog.class, Table.class, View.class, Column.class);
        EmptyDatabaseSnapshot emptyDatabaseSnapshot = new EmptyDatabaseSnapshot(database, control);

        DiffToChangeLog obj = new DiffToChangeLog(new DiffResult(emptyDatabaseSnapshot, emptyDatabaseSnapshot, new CompareControl()), null);

        assertThat("There should be some types", obj.getOrderedOutputTypes(UnexpectedObjectChangeGenerator.class), equalTo(Arrays.asList(
                Catalog.class,
                ForeignKey.class,
                View.class,
                Table.class,
                PrimaryKey.class,
                Column.class
        )));
        assertThat("There should be some types", obj.getOrderedOutputTypes(MissingObjectChangeGenerator.class), equalTo(Arrays.asList(
                Catalog.class,
                Table.class,
                Column.class,
                PrimaryKey.class,
                View.class
        )));
        assertThat("There should be some types", obj.getOrderedOutputTypes(ChangedObjectChangeGenerator.class), equalTo(Arrays.asList(
                Catalog.class,
                Table.class,
                Column.class,
                PrimaryKey.class,
                View.class
        )));
    }

    @Test
    public void generateChangeSets_keepsMissingColumnBeforeForeignKeyWhenDependencySortingIsEnabled() throws Exception {
        MSSQLDatabase referenceDatabase = new MSSQLDatabase();
        referenceDatabase.setConnection(new MockDatabaseConnection());

        MSSQLDatabase comparisonDatabase = new MSSQLDatabase();
        comparisonDatabase.setConnection(new MockDatabaseConnection());

        SnapshotControl control = new SnapshotControl(referenceDatabase, Table.class, Column.class, PrimaryKey.class, Index.class, UniqueConstraint.class, ForeignKey.class);
        EmptyDatabaseSnapshot referenceSnapshot = new EmptyDatabaseSnapshot(referenceDatabase, control);
        EmptyDatabaseSnapshot comparisonSnapshot = new EmptyDatabaseSnapshot(comparisonDatabase, control);

        DiffResult diffResult = new DiffResult(referenceSnapshot, comparisonSnapshot, new CompareControl());

        Table baseTable = new Table(null, "dbo", "Test");
        baseTable.setSnapshotId(SnapshotIdService.getInstance().generateId());
        Table foreignKeyTable = new Table(null, "dbo", "Test2");
        foreignKeyTable.setSnapshotId(SnapshotIdService.getInstance().generateId());

        Column missingColumn = new Column("testID")
                .setRelation(foreignKeyTable)
                .setType(new DataType("bigint"))
                .setNullable(false);

        ForeignKey missingForeignKey = new ForeignKey("FK__Test2__testID__267ABA7A");
        missingForeignKey.setForeignKeyTable(foreignKeyTable);
        missingForeignKey.addForeignKeyColumn(new Column("testID"));
        missingForeignKey.setPrimaryKeyTable(baseTable);
        missingForeignKey.addPrimaryKeyColumn(new Column("ID"));

        diffResult.addMissingObject(missingColumn);
        diffResult.addMissingObject(missingForeignKey);

        DiffToChangeLog diffToChangeLog = new DiffToChangeLog(diffResult, new DiffOutputControl()) {
            @Override
            protected void addDependencies(DependencyUtil.DependencyGraph<String> graph, List<String> schemas, liquibase.database.Database database) {
                graph.add("dbo.Test", "dbo.FK__Test2__testID__267ABA7A");
            }
        };

        List<ChangeSet> changeSets = diffToChangeLog.generateChangeSets();

        assertThat(changeSets, hasSize(2));
        assertThat(changeSets.get(0).getChanges(), hasSize(1));
        assertThat(changeSets.get(1).getChanges(), hasSize(1));

        Change firstChange = changeSets.get(0).getChanges().get(0);
        Change secondChange = changeSets.get(1).getChanges().get(0);

        assertThat(firstChange, instanceOf(AddColumnChange.class));
        assertThat(secondChange, instanceOf(AddForeignKeyConstraintChange.class));
    }
}
