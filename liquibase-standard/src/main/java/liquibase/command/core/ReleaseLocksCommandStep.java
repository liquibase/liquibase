package liquibase.command.core;

import liquibase.Scope;
import liquibase.command.AbstractCommandStep;
import liquibase.command.CommandDefinition;
import liquibase.command.CommandResultsBuilder;
import liquibase.database.Database;
import liquibase.lockservice.LockServiceFactory;

import java.util.Arrays;
import java.util.List;

public class ReleaseLocksCommandStep extends AbstractCommandStep {

    public static final String[] COMMAND_NAME = {"releaseLocks"};

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        Database database = (Database) resultsBuilder.getCommandScope().getDependency(Database.class);
        ListLocksCommandStep.initializeChangelogService(database);
        LockServiceFactory.getInstance().getLockService(database).forceReleaseLock();
        Scope.getCurrentScope().getUI().sendMessage(String.format(
                        coreBundle.getString("successfully.released.database.change.log.locks"),
                        database.getConnection().getConnectionUserName() +
                                "@" + database.getConnection().getURL()
                )
        );
    }

    @Override
    public List<Class<?>> requiredDependencies() {
        return Arrays.asList(Database.class);
    }



    @Override
    public String[][] defineCommandNames() {
        return new String[][] { COMMAND_NAME };
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        commandDefinition.setShortDescription("Remove the Liquibase lock record from the DATABASECHANGELOG table");
    }

}
