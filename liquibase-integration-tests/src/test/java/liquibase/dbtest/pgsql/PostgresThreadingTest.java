package liquibase.dbtest.pgsql;

import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.command.CommandResults;
import liquibase.command.CommandScope;
import liquibase.command.core.StatusCommandStep;
import liquibase.command.core.helpers.DatabaseChangelogCommandStep;
import liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep;
import liquibase.database.DatabaseFactory;
import liquibase.dbtest.AbstractIntegrationTest;
import liquibase.exception.CommandExecutionException;
import liquibase.exception.LiquibaseException;
import liquibase.logging.mdc.customobjects.SimpleStatus;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeNotNull;

public class PostgresThreadingTest extends AbstractIntegrationTest {

    private String dependenciesChangeLog;

    public PostgresThreadingTest() throws Exception {
        super("pgsql", DatabaseFactory.getInstance().getDatabase("postgresql"));
        dependenciesChangeLog = "changelogs/pgsql/pg_sleep.sql";
    }

    @Test
    public void testStatusRunDuringUpdate() throws Exception {
        assumeNotNull(this.getDatabase());

        Liquibase liquibase = createLiquibase(this.dependenciesChangeLog);
        clearDatabase();

        CommandScope commandScope = getStatusCommandScope();
        CommandResults statusCheckBefore = commandScope.execute();

        CompletableFuture<Boolean> updateFuture =
                CompletableFuture.supplyAsync(() -> {
                try {
                    liquibase.update(new Contexts());
                    return true;
                } catch (LiquibaseException e) {
                    throw new RuntimeException(e);
                }
            });

        Thread.sleep(3000); // wait for update to start running
        CommandResults statusCheckDuring = commandScope.execute();

        assertTrue(updateFuture.get());

        CommandResults statusCheckAfter = commandScope.execute();

        assertEquals("undeployed", ((SimpleStatus)statusCheckBefore.getResult("status")).getMessage());
        assertEquals(1, ((SimpleStatus)statusCheckBefore.getResult("status")).getChangesetCount());
        assertEquals("undeployed", ((SimpleStatus)statusCheckDuring.getResult("status")).getMessage());
        assertEquals(1, ((SimpleStatus)statusCheckDuring.getResult("status")).getChangesetCount());
        assertEquals("up-to-date", ((SimpleStatus)statusCheckAfter.getResult("status")).getMessage());
        assertEquals(0, ((SimpleStatus)statusCheckAfter.getResult("status")).getChangesetCount());


    }

    private CommandScope getStatusCommandScope() throws CommandExecutionException {
        CommandScope commandScope = new CommandScope(StatusCommandStep.COMMAND_NAME);
        commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, testSystem.getConnectionUrl());
        commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, testSystem.getUsername());
        commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG, testSystem.getPassword());
        commandScope.addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_FILE_ARG, dependenciesChangeLog);
        return commandScope;
    }

}
