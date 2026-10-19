package liquibase.command.update

import liquibase.GlobalConfiguration
import liquibase.Scope
import liquibase.command.CommandScope
import liquibase.command.core.RollbackCountCommandStep
import liquibase.command.core.UpdateCommandStep
import liquibase.command.core.UpdateCountCommandStep
import liquibase.command.core.helpers.DatabaseChangelogCommandStep
import liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep
import liquibase.command.util.CommandUtil
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import liquibase.resource.SearchPathResourceAccessor
import spock.lang.Shared
import spock.lang.Specification

/**
 * Checks {@code liquibase.atomicHistoryUpdates} on Postgres. A plpgsql trigger on DATABASECHANGELOG raises an
 * exception when changeset 2's row is inserted or deleted, which is right after the changeset's own statements
 * have run.
 */
@LiquibaseIntegrationTest
class AtomicHistoryUpdatesPostgresIntegrationTest extends Specification {

    private static final String ADD_COLUMN = "changelogs/pgsql/update/atomicHistoryUpdates-addColumn.xml"
    private static final String CREATE_TABLE = "changelogs/pgsql/update/atomicHistoryUpdates-createTable.xml"
    private static final String HISTORY_FAILURE = "simulated history failure"

    @Shared
    private DatabaseTestSystem postgres = (DatabaseTestSystem) Scope.getCurrentScope()
            .getSingleton(TestSystemFactory.class)
            .getTestSystem("postgresql")

    /** Drops the trigger function. The framework's drop-all has already removed the tables and their triggers. */
    def cleanup() {
        if (postgres.shouldTest()) {
            postgres.executeSql("DROP FUNCTION IF EXISTS fail_history() CASCADE")
        }
    }

    /** With the setting on, a failed history insert rolls back the changeset's changes, and a retry succeeds. */
    def "with atomicHistoryUpdates on, a failed history insert rolls back the changeset"() {
        given:
        withAtomicHistoryUpdates(true) { updateCount(ADD_COLUMN, 1) }
        failHistoryOn("INSERT")

        when:
        def failure = failureOf { withAtomicHistoryUpdates(true) { update(ADD_COLUMN) } }

        then: "the addColumn and addNotNullConstraint were rolled back with the history row"
        mentions(failure, HISTORY_FAILURE)
        !columns("t1").contains("c2")
        historyIds() == ["1"]

        when: "the history write works again"
        allowHistory()
        withAtomicHistoryUpdates(true) { update(ADD_COLUMN) }

        then:
        columns("t1").contains("c2")
        historyIds() == ["1", "2"]
    }

    /** With the setting on, a failed history delete undoes the rollback's changes, and a retry succeeds. */
    def "with atomicHistoryUpdates on, a failed history delete undoes the rollback"() {
        given:
        withAtomicHistoryUpdates(true) { update(CREATE_TABLE) }
        failHistoryOn("DELETE")

        when:
        def failure = failureOf { withAtomicHistoryUpdates(true) { rollbackCount(CREATE_TABLE, 1) } }

        then: "the DROP TABLE was rolled back with the history delete"
        mentions(failure, HISTORY_FAILURE)
        tableExists("t2")
        historyIds() == ["1", "2"]

        when: "the history delete works again"
        allowHistory()
        withAtomicHistoryUpdates(true) { rollbackCount(CREATE_TABLE, 1) }

        then:
        !tableExists("t2")
        historyIds() == ["1"]
    }

    /** With the setting at its default, a failed history insert leaves the changes applied, so the retry fails. */
    def "with atomicHistoryUpdates off by default, a failed history insert leaves the changeset applied"() {
        given:
        updateCount(ADD_COLUMN, 1)
        failHistoryOn("INSERT")

        when:
        def failure = failureOf { update(ADD_COLUMN) }

        then: "the changeset was committed before its history row"
        mentions(failure, HISTORY_FAILURE)
        columns("t1").contains("c2")
        historyIds() == ["1"]

        when: "the history write works again"
        allowHistory()
        def retryFailure = failureOf { update(ADD_COLUMN) }

        then: "re-running the changeset fails on the column it already added"
        mentions(retryFailure, 'column "c2" of relation "t1" already exists')
    }

    /** With the setting explicitly off, a failed history delete leaves the rollback committed, so the retry fails. */
    def "with atomicHistoryUpdates off, a failed history delete leaves the rollback committed"() {
        given:
        withAtomicHistoryUpdates(false) { update(CREATE_TABLE) }
        failHistoryOn("DELETE")

        when:
        def failure = failureOf { withAtomicHistoryUpdates(false) { rollbackCount(CREATE_TABLE, 1) } }

        then: "the DROP TABLE was committed before the history delete"
        mentions(failure, HISTORY_FAILURE)
        !tableExists("t2")
        historyIds() == ["1", "2"]

        when: "the history delete works again"
        allowHistory()
        def retryFailure = failureOf { withAtomicHistoryUpdates(false) { rollbackCount(CREATE_TABLE, 1) } }

        then: "rolling back again fails on the table it already dropped"
        mentions(retryFailure, 'table "t2" does not exist')
    }

    /** Makes changeset 2's DATABASECHANGELOG insert or delete fail with a plpgsql RAISE EXCEPTION. */
    private void failHistoryOn(String operation) {
        postgres.executeSql('''
            CREATE OR REPLACE FUNCTION fail_history() RETURNS trigger LANGUAGE plpgsql AS $$
            BEGIN
                IF TG_OP = 'INSERT' THEN
                    IF NEW.id = '2' THEN
                        RAISE EXCEPTION 'simulated history failure';
                    END IF;
                    RETURN NEW;
                END IF;
                IF OLD.id = '2' THEN
                    RAISE EXCEPTION 'simulated history failure';
                END IF;
                RETURN OLD;
            END
            $$''')
        postgres.executeSql("CREATE TRIGGER fail_history BEFORE " + operation +
                " ON databasechangelog FOR EACH ROW EXECUTE FUNCTION fail_history()")
    }

    /** Removes the failing trigger and releases any changelog lock the failed run left behind. */
    private void allowHistory() {
        postgres.executeSql("DROP TRIGGER IF EXISTS fail_history ON databasechangelog")
        CommandUtil.runReleaseLocks(postgres)
    }

    /** Runs {@code update} with the given changelog. */
    private void update(String changelog) {
        execute(new CommandScope(UpdateCommandStep.COMMAND_NAME)
                .addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, changelog))
    }

    /** Runs {@code update-count} with the given changelog and count. */
    private void updateCount(String changelog, int count) {
        execute(new CommandScope(UpdateCountCommandStep.COMMAND_NAME)
                .addArgumentValue(UpdateCountCommandStep.CHANGELOG_FILE_ARG, changelog)
                .addArgumentValue(UpdateCountCommandStep.COUNT_ARG, count))
    }

    /** Runs {@code rollback-count} with the given changelog and count. */
    private void rollbackCount(String changelog, int count) {
        execute(new CommandScope(RollbackCountCommandStep.COMMAND_NAME)
                .addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_FILE_ARG, changelog)
                .addArgumentValue(RollbackCountCommandStep.COUNT_ARG, count))
    }

    /** Runs a command against the Postgres test system, resolving changelogs from the test classpath. */
    private void execute(CommandScope commandScope) {
        commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, postgres.getConnectionUrl())
                .addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, postgres.getUsername())
                .addArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG, postgres.getPassword())
        Scope.child([(Scope.Attr.resourceAccessor.name()): new SearchPathResourceAccessor(".,target/test-classes")], {
            commandScope.execute()
        } as Scope.ScopedRunnerWithReturn<Void>)
    }

    /** Runs {@code action} with {@code liquibase.atomicHistoryUpdates} set to {@code enabled}. */
    private static <T> T withAtomicHistoryUpdates(boolean enabled, Closure<T> action) {
        return Scope.child([(GlobalConfiguration.ATOMIC_HISTORY_UPDATES.getKey()): enabled],
                action as Scope.ScopedRunnerWithReturn<T>)
    }

    /** Returns the exception thrown by {@code action}, failing the test if it doesn't throw. */
    private static Throwable failureOf(Closure action) {
        try {
            action()
        } catch (Throwable t) {
            return t
        }
        throw new AssertionError("expected the command to fail")
    }

    /** Returns whether the message of {@code failure} or any of its causes contains {@code text}. */
    private static boolean mentions(Throwable failure, String text) {
        for (Throwable t = failure; t != null; t = t.getCause()) {
            if (t.getMessage()?.contains(text)) {
                return true
            }
        }
        return false
    }

    /** Returns the column names of the given table in the current schema. */
    private List<String> columns(String table) {
        return query("SELECT column_name FROM information_schema.columns " +
                "WHERE table_schema = current_schema() AND table_name = '" + table + "'")
    }

    /** Returns whether the given table exists in the current schema. */
    private boolean tableExists(String table) {
        return !query("SELECT table_name FROM information_schema.tables " +
                "WHERE table_schema = current_schema() AND table_name = '" + table + "'").isEmpty()
    }

    /** Returns the DATABASECHANGELOG ids in execution order. */
    private List<String> historyIds() {
        return query("SELECT id FROM databasechangelog ORDER BY orderexecuted")
    }

    /**
     * Runs a query on the test system's shared connection and commits, so no read locks are held while Liquibase
     * runs DDL on the same tables.
     */
    private List<String> query(String sql) {
        def connection = postgres.getConnection()
        List<String> values = []
        connection.createStatement().withCloseable { statement ->
            statement.executeQuery(sql).withCloseable { rs ->
                while (rs.next()) {
                    values << rs.getString(1)
                }
            }
        }
        if (!connection.getAutoCommit()) {
            connection.commit()
        }
        return values
    }
}
