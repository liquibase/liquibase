package liquibase.command.core;

import liquibase.Scope;
import liquibase.changelog.ChangeLogHistoryService;
import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.command.AbstractCommandStep;
import liquibase.command.CommandDefinition;
import liquibase.command.CommandResultsBuilder;
import liquibase.command.CommandScope;
import liquibase.database.Database;
import liquibase.lockservice.LockService;

import java.util.Arrays;
import java.util.List;

public class ClearChecksumsCommandStep extends AbstractCommandStep {

    public static final String[] COMMAND_NAME = {"clearChecksums"};

    @Override
    public String[][] defineCommandNames() {
        return new String[][] { COMMAND_NAME };
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        commandDefinition.setShortDescription("Clears all checksums");
        commandDefinition.setLongDescription("Clears all checksums and nullifies the MD5SUM column of the " +
                "DATABASECHANGELOG table so that they will be re-computed on the next database update");
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        CommandScope commandScope = resultsBuilder.getCommandScope();
        final Database database = (Database) commandScope.getDependency(Database.class);

        ChangeLogHistoryService changeLogHistoryService = Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class).getChangeLogService(database);
        changeLogHistoryService.init();
        Scope.getCurrentScope().getLog(getClass()).info(String.format("Clearing database change log checksums for database %s", database.getShortName()));
        changeLogHistoryService.clearAllCheckSums();
    }

    @Override
    public List<Class<?>> requiredDependencies() {
        return Arrays.asList(Database.class, LockService.class);
    }

}
