package liquibase.command.core;

import liquibase.RuntimeEnvironment;
import liquibase.changelog.*;
import liquibase.changelog.filter.*;
import liquibase.command.*;
import liquibase.database.Database;
import liquibase.exception.ChangeLogParseException;

import java.util.*;

public class ValidateCommandStep extends AbstractCommandStep {

    public static final String[] COMMAND_NAME = {"validate"};

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
        return Arrays.asList(Database.class, DatabaseChangeLog.class);
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        CommandScope commandScope = resultsBuilder.getCommandScope();
        final DatabaseChangeLog databaseChangeLog = (DatabaseChangeLog) commandScope.getDependency(DatabaseChangeLog.class);
        final Database database = (Database) commandScope.getDependency(Database.class);

        ValidateChangeLogIterator validateChangeLogIterator = getValidateChangelogIterator(databaseChangeLog, database);
        validateChangeLogIterator.run(null, new RuntimeEnvironment(database, null, null));
        List<ChangeSetFilterResult> reasons = validateChangeLogIterator.getReasonsDenied();

        processDeniedFilterResultsIfAny(resultsBuilder, reasons);
        resultsBuilder.addResult("statusCode", 0);
    }

    private static void processDeniedFilterResultsIfAny(CommandResultsBuilder resultsBuilder, List<ChangeSetFilterResult> reasons) throws ChangeLogParseException {
        if (reasons != null && !reasons.isEmpty()) {
            StringBuilder validateMessage = new StringBuilder("Execution cannot continue because validation errors have been found: \n");
            for (ChangeSetFilterResult failedFilterResult : reasons) {
                validateMessage.append(String.format("- Property: %s Error: %s", failedFilterResult.getDisplayName(), failedFilterResult.getMessage())).append("\n");
            }
            resultsBuilder.addResult("statusCode", 1);
            throw new ChangeLogParseException(validateMessage.toString());
        }
    }

    private ValidateChangeLogIterator getValidateChangelogIterator(DatabaseChangeLog changeLog, Database database) {
        List<ChangeSetFilter> changesetFilters = this.getValidateChangelogIteratorFilters(database);
        return new ValidateChangeLogIterator(changeLog, changesetFilters.toArray(new ChangeSetFilter[0]));
    }

    private List<ChangeSetFilter> getValidateChangelogIteratorFilters(Database database) {
        List<ChangeSetFilter> filters = new ArrayList<>();
        filters.add(new LabelChangeSetFilter());
        filters.add(new ContextChangeSetFilter());
        filters.add(new RunWithChangeSetFilter());
        filters.add(new DbmsChangeSetFilter(database));
        filters.add(new LogicalFilePathChangeSetFilter());
        return filters;
    }

}
