package liquibase.command.core;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.changelog.ChangeLogHistoryService;
import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.command.*;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import liquibase.lockservice.DatabaseChangeLogLock;
import liquibase.lockservice.LockServiceFactory;

import java.io.PrintStream;
import java.text.DateFormat;
import java.util.Collections;
import java.util.List;

public class ListLocksCommandStep extends AbstractCommandStep {

    public static final String[] COMMAND_NAME = {"listLocks"};

    public static final CommandArgumentDefinition<String> CHANGELOG_FILE_ARG;
    public static final CommandArgumentDefinition<PrintStream> PRINT_STREAM;

    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        CHANGELOG_FILE_ARG = builder.argument(CommonArgumentNames.CHANGELOG_FILE, String.class)
                .description("The root changelog").build();
        PRINT_STREAM = builder.argument("printStream", PrintStream.class)
                .hidden()
                .defaultValue(System.err)
                .build();
    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][] { COMMAND_NAME };
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        commandDefinition.setShortDescription("List the hostname, IP address, and timestamp of the Liquibase lock record");
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        CommandScope commandScope = resultsBuilder.getCommandScope();
        reportLocks(commandScope.getArgumentValue(PRINT_STREAM), (Database) commandScope.getDependency(Database.class));
        resultsBuilder.addResult("statusCode", 0);
    }

    @Override
    public List<Class<?>> requiredDependencies() {
        return Collections.singletonList(Database.class);
    }

    public static void reportLocks(PrintStream out, Database database) throws LiquibaseException {
        DatabaseChangeLogLock[] locks = listLocks(database);
        out.println("Database change log locks for " + database.getConnection().getConnectionUserName()
                + "@" + database.getConnection().getURL());
        if (locks.length == 0) {
            out.println(" - No locks");
            return;
        }
        for (DatabaseChangeLogLock lock : locks) {
            out.println(" - " + lock.getLockedBy() + " at " +
                    DateFormat.getDateTimeInstance().format(lock.getLockGranted()));
        }
        out.println("NOTE:  The lock time displayed is based on the database's configured time");
    }

    /**
     * Display change log lock information.
     */
    public static DatabaseChangeLogLock[] listLocks(Database database) throws LiquibaseException {
        checkLiquibaseTables(false, null, new Contexts(), new LabelExpression(), database);

        return LockServiceFactory.getInstance().getLockService(database).listLocks();
    }

    public static void checkLiquibaseTables(boolean updateExistingNullChecksums, DatabaseChangeLog databaseChangeLog,
                                     Contexts contexts, LabelExpression labelExpression, Database database) throws LiquibaseException {
        ChangeLogHistoryService changeLogHistoryService =
                ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(database);
        changeLogHistoryService.init();
        if (updateExistingNullChecksums) {
            changeLogHistoryService.upgradeChecksums(databaseChangeLog, contexts, labelExpression);
        }
        LockServiceFactory.getInstance().getLockService(database).init();
    }
}
