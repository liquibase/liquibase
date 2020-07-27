package liquibase.diff.output.changelog;

import liquibase.database.core.MySQLDatabase;
import liquibase.diff.DiffResult;
import liquibase.diff.compare.CompareControl;
import liquibase.snapshot.EmptyDatabaseSnapshot;
import liquibase.snapshot.SnapshotControl;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class DiffToChangeLogTest {
    @Test
    public void getOrderedOutputTypes_isConsistant() throws Exception {
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
        Class<? extends DatabaseObject>[] typesArray = new Class[5];
        typesArray[0] = Schema.class;
        typesArray[1] = View.class;
        typesArray[2] = Catalog.class;
        typesArray[3] = Table.class;
        typesArray[4] = Column.class;
        SnapshotControl control = new SnapshotControl(database, typesArray);
        EmptyDatabaseSnapshot emptyDatabaseSnapshot = new EmptyDatabaseSnapshot(database, control);
        DiffToChangeLog obj = new DiffToChangeLog(new DiffResult(emptyDatabaseSnapshot, emptyDatabaseSnapshot, new CompareControl()), null);

        for (Class<? extends ChangeGenerator> type : new Class[] {UnexpectedObjectChangeGenerator.class, MissingObjectChangeGenerator.class, ChangedObjectChangeGenerator.class}) {
            List<Class<? extends DatabaseObject>> orderedOutputTypes = obj.getOrderedOutputTypes(type);
            assertThat("There should be some types", orderedOutputTypes, hasSize(greaterThan(5)));
        }
        List<Class<? extends DatabaseObject>> unexpectedOrderedOutputTypes = obj.getOrderedOutputTypes(UnexpectedObjectChangeGenerator.class);
        assertThat("There should be some types", unexpectedOrderedOutputTypes, hasSize(7));
        List<Class<? extends DatabaseObject>> missingOrderedOutputTypes = obj.getOrderedOutputTypes(MissingObjectChangeGenerator.class);
        assertThat("There should be some types", missingOrderedOutputTypes, hasSize(6));
        List<Class<? extends DatabaseObject>> changedOrderedOutputTypes = obj.getOrderedOutputTypes(ChangedObjectChangeGenerator.class);
        assertThat("There should be some types", changedOrderedOutputTypes, hasSize(6));
    }
}
