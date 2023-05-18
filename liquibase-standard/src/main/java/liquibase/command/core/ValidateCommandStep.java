package liquibase.command.core;

import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.command.*;
import liquibase.command.core.helpers.DatabaseChangelogCommandStep;
import liquibase.database.Database;
import liquibase.exception.CommandValidationException;

import java.util.Arrays;
import java.util.List;

public class ValidateCommandStep extends AbstractCommandStep {

    public static final String[] COMMAND_NAME = {"validate"};

    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][]{COMMAND_NAME};
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        commandDefinition.setShortDescription("Validate the changelog for errors");
    }

    @Override
    public List<Class<?>> requiredDependencies() {
        return Arrays.asList(Database.class, DatabaseChangeLog.class, ChangeLogParameters.class);
    }

    @Override
    public void validate(CommandScope commandScope) throws CommandValidationException {
        // Do not update checksums when running validate.
        commandScope.addArgumentValue(DatabaseChangelogCommandStep.UPDATE_CHECKSUMS, Boolean.FALSE);
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        final CommandScope commandScope = resultsBuilder.getCommandScope();
        final DatabaseChangeLog changeLog = (DatabaseChangeLog) commandScope.getDependency(DatabaseChangeLog.class);
        final Database database = (Database) commandScope.getDependency(Database.class);
        if (changeLog != null) {
            changeLog.validate(database);
        }
        resultsBuilder.addResult("statusCode", 0);
    }
}
