package liquibase.command.core;

import liquibase.Scope;
import liquibase.changelog.RanChangeSet;
import liquibase.changelog.filter.ExecutedAfterChangeSetFilter;
import liquibase.command.*;
import liquibase.database.Database;
import liquibase.logging.mdc.MdcKey;

import java.util.Date;
import java.util.List;

public class RollbackToDateCommandStep extends AbstractRollbackCommandStep {

    public static final String[] COMMAND_NAME = {"rollbackToDate"};

    public static final CommandArgumentDefinition<Date> DATE_ARG;

    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        DATE_ARG = builder.argument("date", Date.class).required()
            .description("Date to rollback changes to").build();

        builder.addArgument(AbstractRollbackCommandStep.ROLLBACK_SCRIPT_ARG).build();
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        Scope.getCurrentScope().addMdcValue(MdcKey.LIQUIBASE_OPERATION, COMMAND_NAME[0]);
        Scope.getCurrentScope().addMdcValue(MdcKey.LIQUIBASE_COMMAND_NAME, COMMAND_NAME[0]);
        CommandScope commandScope = resultsBuilder.getCommandScope();
        Date dateToRollBackTo = commandScope.getArgumentValue(DATE_ARG);
        Scope.getCurrentScope().addMdcValue(MdcKey.ROLLBACK_TO_DATE, dateToRollBackTo.toString());

        Database database = (Database) commandScope.getDependency(Database.class);

        List<RanChangeSet> ranChangeSetList = database.getRanChangeSetList();
        this.doRollback(resultsBuilder, new ExecutedAfterChangeSetFilter(dateToRollBackTo, ranChangeSetList), ranChangeSetList);
    }

    @Override
    protected String getOperationCommand() {
        return "rollbackToDate";
    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][] { COMMAND_NAME };
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        commandDefinition.setShortDescription("Rollback changes made to the database based on the specific date");
    }
}
