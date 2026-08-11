package liquibase.diff.output.changelog;

import liquibase.change.Change;
import liquibase.change.core.AddForeignKeyConstraintChange;
import liquibase.change.core.CreateTableChange;
import liquibase.change.core.DropForeignKeyConstraintChange;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.OfflineConnection;
import liquibase.database.core.CockroachDatabase;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.diff.DiffResult;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.DiffOutputControl;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.EmptyDatabaseSnapshot;
import liquibase.snapshot.SnapshotControl;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.*;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DiffToChangeLogTest {

    /**
     * convertStoredLogicObjectName is private static, so reflection is the only way to cover it directly. It is
     * reached from getOrderedOutputTypes' dependency ordering, which needs a live Postgres connection.
     */
    private static String convertStoredLogicObjectName(String schemaName, String objectName, Database database)
            throws Exception {
        Method method = DiffToChangeLog.class.getDeclaredMethod(
                "convertStoredLogicObjectName", String.class, String.class, Database.class);
        method.setAccessible(true);
        return (String) method.invoke(null, schemaName, objectName, database);
    }

    @Test
    public void convertStoredLogicObjectName_rewritesParameterListsToTypesOnly() throws Exception {
        final PostgresDatabase database = new PostgresDatabase();

        assertThat(convertStoredLogicObjectName("public", "calculate_bonus(emp_salary numeric, emp_name character varying)", database),
                is("public.calculate_bonus(numeric,character varying)"));
        assertThat(convertStoredLogicObjectName("public", "one_arg(x integer)", database),
                is("public.one_arg(integer)"));
    }

    @Test
    public void convertStoredLogicObjectName_leavesNoArgumentSignaturesAlone() throws Exception {
        final PostgresDatabase database = new PostgresDatabase();

        // Nothing to rewrite between the parentheses, so the name is only schema-qualified
        assertThat(convertStoredLogicObjectName("public", "no_args()", database), is("public.no_args()"));
        assertThat(convertStoredLogicObjectName("public", "spaced( )", database), is("public.spaced( )"));
    }

    @Test
    public void convertStoredLogicObjectName_onlyAppliesToPostgres() throws Exception {
        assertThat(convertStoredLogicObjectName("public", "calculate_bonus(emp_salary numeric)", new MySQLDatabase()),
                is("public.calculate_bonus(emp_salary numeric)"));
    }

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

    // --- liveDatabaseForSerialization: returns the live DB only for online Postgres-family connections ---

    @Test
    public void liveDatabaseForSerialization_onlinePostgres_returnsDatabase() {
        assertLiveDatabase(mock(PostgresDatabase.class), mock(JdbcConnection.class), true);
    }

    @Test
    public void liveDatabaseForSerialization_onlineCockroach_returnsDatabase() { // whole PG family, not just real Postgres
        assertLiveDatabase(mock(CockroachDatabase.class), mock(JdbcConnection.class), true);
    }

    @Test
    public void liveDatabaseForSerialization_offlinePostgres_returnsNull() {
        assertLiveDatabase(mock(PostgresDatabase.class), mock(OfflineConnection.class), false);
    }

    @Test
    public void liveDatabaseForSerialization_onlineNonPostgres_returnsNull() {
        assertLiveDatabase(mock(MySQLDatabase.class), mock(JdbcConnection.class), false);
    }

    private void assertLiveDatabase(Database database, DatabaseConnection connection, boolean expectReturned) {
        when(database.getConnection()).thenReturn(connection);
        DatabaseSnapshot snapshot = mock(DatabaseSnapshot.class);
        when(snapshot.getDatabase()).thenReturn(database);

        Database result = new DiffToChangeLog((DiffOutputControl) null).liveDatabaseForSerialization(snapshot);

        assertThat(result, expectReturned ? sameInstance(database) : nullValue());
    }

    // --- bringDropFKToTop: drop-FK changesets float to the top, order preserved within each group ---

    @Test
    public void bringDropFKToTop_movesDropFKFirst_preservingOrderWithinGroups() {
        ChangeSet addA = changeSetWith(new AddForeignKeyConstraintChange());
        ChangeSet dropA = changeSetWith(new DropForeignKeyConstraintChange());
        ChangeSet addB = changeSetWith(new CreateTableChange());
        ChangeSet dropB = changeSetWith(new DropForeignKeyConstraintChange());

        List<ChangeSet> result = new DiffToChangeLog((DiffOutputControl) null)
                .bringDropFKToTop(Arrays.asList(addA, dropA, addB, dropB));

        assertThat(result, contains(dropA, dropB, addA, addB));
    }

    @Test
    public void bringDropFKToTop_returnsInputUnchanged_whenNoDropFK() {
        ChangeSet addA = changeSetWith(new AddForeignKeyConstraintChange());
        ChangeSet addB = changeSetWith(new CreateTableChange());

        List<ChangeSet> result = new DiffToChangeLog((DiffOutputControl) null)
                .bringDropFKToTop(Arrays.asList(addA, addB));

        assertThat(result, contains(addA, addB));
    }

    private ChangeSet changeSetWith(Change change) {
        ChangeSet cs = mock(ChangeSet.class);
        when(cs.getChanges()).thenReturn(Collections.singletonList(change));
        return cs;
    }
}
