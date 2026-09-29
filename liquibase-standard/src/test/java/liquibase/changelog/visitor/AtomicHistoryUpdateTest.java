package liquibase.changelog.visitor;

import liquibase.GlobalConfiguration;
import liquibase.Liquibase;
import liquibase.Scope;
import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.lockservice.LockServiceFactory;
import liquibase.resource.DirectoryResourceAccessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Uses SQLite because it supports DDL inside transactions, and a trigger on DATABASECHANGELOG to make the
 * history write fail right after the changeset's own statements have run.
 */
class AtomicHistoryUpdateTest {

    private static final String FAIL_HISTORY_INSERT =
            "CREATE TRIGGER fail_history BEFORE INSERT ON DATABASECHANGELOG WHEN NEW.ID = '2' " +
            "BEGIN SELECT RAISE(ABORT, 'simulated failure writing history'); END";
    private static final String FAIL_HISTORY_DELETE =
            "CREATE TRIGGER fail_history BEFORE DELETE ON DATABASECHANGELOG WHEN OLD.ID = '2' " +
            "BEGIN SELECT RAISE(ABORT, 'simulated failure removing history'); END";

    @TempDir
    Path dir;

    private String url;

    /** Points each test at a fresh SQLite file and writes the changelogs the tests use. */
    @BeforeEach
    void setUp() throws Exception {
        url = "jdbc:sqlite:" + dir.resolve("test.db");
        String createT1 = changeSet("1", "", "<createTable tableName=\"t1\"><column name=\"id\" type=\"int\"/></createTable>");
        String addColumn = "<addColumn tableName=\"t1\"><column name=\"c2\" type=\"varchar(10)\"/></addColumn>";
        writeChangeLog("step1.xml", createT1);
        writeChangeLog("add-column.xml", createT1, changeSet("2", "", addColumn));
        writeChangeLog("add-column-no-tx.xml", createT1, changeSet("2", " runInTransaction=\"false\"", addColumn));
        writeChangeLog("create-t2.xml", createT1,
                changeSet("2", "", "<createTable tableName=\"t2\"><column name=\"id\" type=\"int\"/></createTable>"));
    }

    /** Disables the non-atomic test history service and resets the history and lock service factories. */
    @AfterEach
    void tearDown() {
        NonAtomicTestChangeLogHistoryService.enabled = false;
        Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class).resetAll();
        LockServiceFactory.getInstance().resetAll();
    }

    // --- liquibase.atomicHistoryUpdates=true ---

    /** With the setting on, a failed history insert rolls back the changeset's changes, and a retry succeeds. */
    @Test
    void updateRollsBackChangeSetWhenHistoryRowCannotBeWritten() throws Exception {
        withAtomicHistoryUpdates(true, () -> {
            update("step1.xml");
            sql(FAIL_HISTORY_INSERT);

            assertThrows(Exception.class, () -> update("add-column.xml"));

            assertFalse(columns("t1").contains("c2"), "the addColumn must be rolled back together with the failed history row");
            assertEquals(Arrays.asList("1"), historyIds());

            sql("DROP TRIGGER fail_history");
            update("add-column.xml");

            assertTrue(columns("t1").contains("c2"));
            assertEquals(Arrays.asList("1", "2"), historyIds());
        });
    }

    /** With the setting on, a failed history delete undoes the rollback's changes, and a retry succeeds. */
    @Test
    void rollbackIsUndoneWhenHistoryRowCannotBeRemoved() throws Exception {
        withAtomicHistoryUpdates(true, () -> {
            update("create-t2.xml");
            sql(FAIL_HISTORY_DELETE);

            assertThrows(Exception.class, () -> rollbackOne("create-t2.xml"));

            assertTrue(tableExists("t2"), "the rollback DDL must be undone together with the failed history delete");
            assertEquals(Arrays.asList("1", "2"), historyIds());

            sql("DROP TRIGGER fail_history");
            rollbackOne("create-t2.xml");

            assertFalse(tableExists("t2"));
            assertEquals(Arrays.asList("1"), historyIds());
        });
    }

    /** With the setting on, a runInTransaction="false" changeset still commits its changes as they run. */
    @Test
    void updateKeepsChangesOfNonTransactionalChangeSetWhenHistoryRowCannotBeWritten() throws Exception {
        withAtomicHistoryUpdates(true, () -> {
            update("step1.xml");
            sql(FAIL_HISTORY_INSERT);

            assertThrows(Exception.class, () -> update("add-column-no-tx.xml"));

            assertTrue(columns("t1").contains("c2"), "runInTransaction=false changes are committed as they run, as before");
            assertEquals(Arrays.asList("1"), historyIds());
        });
    }

    /** With the setting on, a history service that doesn't opt in keeps the two-commit behavior. */
    @Test
    void updateKeepsTwoCommitsWhenHistoryServiceDoesNotSupportAtomicUpdates() throws Exception {
        withAtomicHistoryUpdates(true, () -> {
            NonAtomicTestChangeLogHistoryService.enabled = true;
            NonAtomicTestChangeLogHistoryService.used = false;
            update("step1.xml");
            sql(FAIL_HISTORY_INSERT);

            assertThrows(Exception.class, () -> update("add-column.xml"));

            assertTrue(NonAtomicTestChangeLogHistoryService.used, "the non-atomic history service should have been selected");
            assertTrue(columns("t1").contains("c2"), "history services that do not opt in keep the existing behavior");
            assertEquals(Arrays.asList("1"), historyIds());
        });
    }

    /** With the setting on, the helper also requires runInTransaction=true and a database with transactional DDL. */
    @Test
    void commitsWithHistoryOnlyForTransactionalChangeSetsOnDatabasesWithTransactionalDdl() throws Exception {
        ChangeSet inTransaction = new ChangeSet("1", "test", false, false, "test.xml", null, null, true, null);
        ChangeSet notInTransaction = new ChangeSet("2", "test", false, false, "test.xml", null, null, false, null);
        Database transactionalDdl = databaseWithTransactionalDdl(true);
        Database nonTransactionalDdl = databaseWithTransactionalDdl(false);

        withAtomicHistoryUpdates(true, () -> {
            assertTrue(HistoryTransactionSupport.commitsWithHistory(inTransaction, transactionalDdl));
            assertFalse(HistoryTransactionSupport.commitsWithHistory(notInTransaction, transactionalDdl));
            assertFalse(HistoryTransactionSupport.commitsWithHistory(inTransaction, nonTransactionalDdl));
        });
    }

    // --- liquibase.atomicHistoryUpdates=false (the default) ---

    /** The setting defaults to false. */
    @Test
    void atomicHistoryUpdatesIsDisabledByDefault() {
        assertFalse(GlobalConfiguration.ATOMIC_HISTORY_UPDATES.getDefaultValue());
        assertFalse(GlobalConfiguration.ATOMIC_HISTORY_UPDATES.getCurrentValue());
    }

    /**
     * With the setting at its default, a failed history insert leaves the changes applied and a re-run fails, as before.
     */
    @Test
    void updateLeavesChangeSetAppliedWithoutHistoryRowByDefault() throws Exception {
        update("step1.xml");
        sql(FAIL_HISTORY_INSERT);

        assertThrows(Exception.class, () -> update("add-column.xml"));

        assertTrue(columns("t1").contains("c2"), "without the setting the addColumn is committed before the history row");
        assertEquals(Arrays.asList("1"), historyIds());

        sql("DROP TRIGGER fail_history");
        assertThrows(Exception.class, () -> update("add-column.xml"),
                "re-running re-issues the addColumn against a column that already exists");
    }

    /**
     * With the setting explicitly off, a failed history delete leaves the rollback committed and a retry fails, as before.
     */
    @Test
    void rollbackLeavesChangeSetRolledBackWithHistoryRowWhenDisabled() throws Exception {
        withAtomicHistoryUpdates(false, () -> {
            update("create-t2.xml");
            sql(FAIL_HISTORY_DELETE);

            assertThrows(Exception.class, () -> rollbackOne("create-t2.xml"));

            assertFalse(tableExists("t2"), "with the setting off the DROP TABLE is committed before the history delete");
            assertEquals(Arrays.asList("1", "2"), historyIds());

            sql("DROP TRIGGER fail_history");
            assertThrows(Exception.class, () -> rollbackOne("create-t2.xml"),
                    "retrying re-issues the DROP TABLE against a table that no longer exists");
        });
    }

    /**
     * With the setting off, by default or explicitly, the helper returns false even when every other condition holds.
     */
    @Test
    void commitsWithHistoryIsFalseWhenDisabled() throws Exception {
        ChangeSet inTransaction = new ChangeSet("1", "test", false, false, "test.xml", null, null, true, null);
        Database transactionalDdl = databaseWithTransactionalDdl(true);

        assertFalse(HistoryTransactionSupport.commitsWithHistory(inTransaction, transactionalDdl), "default");
        withAtomicHistoryUpdates(false, () ->
                assertFalse(HistoryTransactionSupport.commitsWithHistory(inTransaction, transactionalDdl), "explicitly false"));
    }

    /** Runs {@code test} with liquibase.atomicHistoryUpdates set to {@code enabled}. */
    private static void withAtomicHistoryUpdates(boolean enabled, Scope.ScopedRunner<?> test) throws Exception {
        Scope.child(GlobalConfiguration.ATOMIC_HISTORY_UPDATES.getKey(), enabled, test);
    }

    /** Returns a mock database whose supportsDDLInTransaction() returns {@code supported}. */
    private static Database databaseWithTransactionalDdl(boolean supported) {
        Database database = mock(Database.class);
        when(database.supportsDDLInTransaction()).thenReturn(supported);
        return database;
    }

    /** Runs update with the given changelog against the test database. */
    private void update(String changeLog) throws Exception {
        try (Liquibase liquibase = liquibase(changeLog)) {
            liquibase.update("");
        }
    }

    /** Rolls back the most recent changeset in the given changelog. */
    private void rollbackOne(String changeLog) throws Exception {
        try (Liquibase liquibase = liquibase(changeLog)) {
            liquibase.rollback(1, (String) null);
        }
    }

    /** Creates a Liquibase instance for the given changelog on a new connection to the test database. */
    private Liquibase liquibase(String changeLog) throws Exception {
        Database database = DatabaseFactory.getInstance()
                .findCorrectDatabaseImplementation(new JdbcConnection(DriverManager.getConnection(url)));
        return new Liquibase(changeLog, new DirectoryResourceAccessor(dir), database);
    }

    /** Executes a statement on its own connection, outside Liquibase. */
    private void sql(String statement) throws SQLException {
        try (Connection connection = DriverManager.getConnection(url); Statement s = connection.createStatement()) {
            s.execute(statement);
        }
    }

    /** Returns the column names of the given table. */
    private List<String> columns(String table) throws SQLException {
        return query("PRAGMA table_info(" + table + ")", "name");
    }

    /** Returns whether the given table exists. */
    private boolean tableExists(String table) throws SQLException {
        return query("SELECT name FROM sqlite_master WHERE type = 'table' AND name = '" + table + "'", "name")
                .contains(table);
    }

    /** Returns the DATABASECHANGELOG ids in execution order. */
    private List<String> historyIds() throws SQLException {
        return query("SELECT ID FROM DATABASECHANGELOG ORDER BY ORDEREXECUTED", "ID");
    }

    /** Runs a query on its own connection and returns the given column from every row. */
    private List<String> query(String sql, String column) throws SQLException {
        List<String> values = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(url);
             Statement s = connection.createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) {
                values.add(rs.getString(column));
            }
        }
        return values;
    }

    /** Returns the XML for a changeset with the given id, extra attributes and body. */
    private static String changeSet(String id, String attributes, String body) {
        return "<changeSet id=\"" + id + "\" author=\"test\" logicalFilePath=\"changelog.xml\"" + attributes + ">" +
                body + "</changeSet>";
    }

    /** Writes a changelog file with the given changesets into the test directory. */
    private void writeChangeLog(String name, String... changeSets) throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<databaseChangeLog xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\"\n" +
                "    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "    xsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog " +
                "http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd\">\n" +
                String.join("\n", changeSets) +
                "\n</databaseChangeLog>\n";
        Files.write(dir.resolve(name), xml.getBytes(StandardCharsets.UTF_8));
    }
}
