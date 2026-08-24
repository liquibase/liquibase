package liquibase.diff.output.changelog;

import liquibase.change.Change;
import liquibase.change.core.AddColumnChange;
import liquibase.change.core.AddForeignKeyConstraintChange;
import liquibase.change.core.CreateTableChange;
import liquibase.change.core.DropForeignKeyConstraintChange;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.MockDatabaseConnection;
import liquibase.database.OfflineConnection;
import liquibase.database.core.CockroachDatabase;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.diff.DiffResult;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.DiffOutputControl;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.EmptyDatabaseSnapshot;
import liquibase.snapshot.SnapshotControl;
import liquibase.snapshot.SnapshotIdService;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.*;
import liquibase.util.DependencyUtil;
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

    @Test
    public void generateChangeSets_doesNotHoistAForeignKeyColumnAheadOfItsOwnTableCreation() throws Exception {
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

        // Unlike the test above, the table holding the foreign key column is missing too, so it is created by this
        // same changelog. Hoisting the column ahead of the sorted block would emit ALTER TABLE before CREATE TABLE.
        diffResult.addMissingObject(foreignKeyTable);
        diffResult.addMissingObject(missingColumn);
        diffResult.addMissingObject(missingForeignKey);

        DiffToChangeLog diffToChangeLog = new DiffToChangeLog(diffResult, new DiffOutputControl()) {
            @Override
            protected void addDependencies(DependencyUtil.DependencyGraph<String> graph, List<String> schemas, liquibase.database.Database database) {
                graph.add("dbo.Test", "dbo.FK__Test2__testID__267ABA7A");
            }
        };

        List<ChangeSet> changeSets = diffToChangeLog.generateChangeSets();

        int createTableIndex = indexOfFirstChange(changeSets, CreateTableChange.class);
        int addColumnIndex = indexOfFirstChange(changeSets, AddColumnChange.class);

        assertThat("the table must be created by this changelog", createTableIndex, greaterThanOrEqualTo(0));
        if (addColumnIndex >= 0) {
            assertThat("a column cannot be added before its own table is created",
                    addColumnIndex, greaterThan(createTableIndex));
        }
    }

    private static int indexOfFirstChange(List<ChangeSet> changeSets, Class<? extends Change> changeType) {
        for (int i = 0; i < changeSets.size(); i++) {
            for (Change change : changeSets.get(i).getChanges()) {
                if (changeType.isInstance(change)) {
                    return i;
                }
            }
        }
        return -1;
    }
}
