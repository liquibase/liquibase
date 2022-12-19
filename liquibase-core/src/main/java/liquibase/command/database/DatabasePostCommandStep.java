package liquibase.command.database;

import liquibase.Scope;
import liquibase.command.*;
import liquibase.command.core.TagCommandStep;
import liquibase.database.Database;
import liquibase.servicelocator.LiquibaseService;
import liquibase.util.BooleanUtil;

import java.util.ResourceBundle;

import static java.util.ResourceBundle.getBundle;

@LiquibaseService(skip = true)
public class DatabasePostCommandStep extends AbstractCommandStep {

    public static final String[] COMMAND_NAME = {"databasePostStep"};

    public static final CommandArgumentDefinition<Database> DATABASE_ARG;
    public static final CommandArgumentDefinition<Boolean> CLOSE_DATABASE_AFTER_COMMAND_ARG;

    private static final ResourceBundle coreBundle = getBundle("liquibase/i18n/liquibase-core");

    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        DATABASE_ARG = builder.argument("database", Database.class).hidden().build();
        CLOSE_DATABASE_AFTER_COMMAND_ARG = builder.argument("closeDatabaseAfterCommand", Boolean.class).hidden().build();
    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][] { COMMAND_NAME };
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        super.adjustCommandDefinition(commandDefinition);
        if (commandDefinition.getPipeline().size() == 1) {
            commandDefinition.setInternal(true);
        }
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        CommandScope commandScope = resultsBuilder.getCommandScope();
        this.closeDatabase(commandScope);
    }

    /**
     * Closes database connection.
     *
     * @param commandScope CommandScope to close database connection if required.
     */
    private void closeDatabase(CommandScope commandScope) {
        Database database = commandScope.getArgumentValue(DatabasePreCommandStep.DATABASE_ARG);
        if (database != null) {
            Boolean closeDb = commandScope.getArgumentValue(CLOSE_DATABASE_AFTER_COMMAND_ARG);
            if (BooleanUtil.isTrue(closeDb)) {
                try {
                    database.close();
                } catch (Exception e) {
                    Scope.getCurrentScope().getLog(getClass()).warning(
                            coreBundle.getString("problem.closing.connection"), e);
                }
            }
        }
    }

    @Override
    public int getOrder(CommandDefinition commandDefinition) {
        if (commandDefinition.is(TagCommandStep.COMMAND_NAME)) {
            return CommandStep.ORDER_LAST;
        } else {
            return super.getOrder(commandDefinition);
        }
    }
}
