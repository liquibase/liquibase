package liquibase.diff.output.changelog;

import liquibase.database.core.MySQLDatabase;
import liquibase.diff.DiffResult;
import liquibase.diff.compare.CompareControl;
import liquibase.snapshot.EmptyDatabaseSnapshot;
import liquibase.snapshot.SnapshotControl;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.*;
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
}
