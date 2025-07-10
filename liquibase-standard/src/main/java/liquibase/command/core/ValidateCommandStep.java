package liquibase.command.core;

import liquibase.Scope;
import liquibase.changelog.*;
import liquibase.changelog.filter.*;
import liquibase.changelog.filter.propertyvalidator.*;
import liquibase.command.*;
import liquibase.database.Database;
import liquibase.exception.ChangeLogParseException;

import java.util.*;

public class ValidateCommandStep extends AbstractCommandStep {

    public static final String[] COMMAND_NAME = {"validate"};
    public static final CommandArgumentDefinition<String> CHANGELOG_FILE_ARG;

    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        CHANGELOG_FILE_ARG = builder.argument(CommonArgumentNames.CHANGELOG_FILE, String.class).required()
                .description("The root changelog file").build();
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
        return Arrays.asList(Database.class);
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        CommandScope commandScope = resultsBuilder.getCommandScope();
        final String changeLogFile = commandScope.getArgumentValue(CHANGELOG_FILE_ARG);
        ValidateChangeLogIterator validateChangeLogIterator = getValidateChangelogIterator(new DatabaseChangeLog(changeLogFile));
        validateChangeLogIterator.run();
        List<ChangeSetFilterResult> reasons = validateChangeLogIterator.getValidationErrors();

        processDeniedFilterResultsIfAny(resultsBuilder, reasons);
        resultsBuilder.addResult("statusCode", 0);
    }

    private static void processDeniedFilterResultsIfAny(CommandResultsBuilder resultsBuilder, List<ChangeSetFilterResult> reasons) throws ChangeLogParseException {
        if (reasons != null && !reasons.isEmpty()) {
            StringBuilder validateMessage = new StringBuilder("Execution cannot continue because validation errors have been found: \n");
            for (ChangeSetFilterResult failedFilterResult : reasons) {
                validateMessage.append(String.format("- Property: %s %n\tError(s): %s", failedFilterResult.getDisplayName(), failedFilterResult.getMessage())).append("\n");
            }
            resultsBuilder.addResult("statusCode", 1);
            throw new ChangeLogParseException(validateMessage.toString());
        } else {
            Scope.getCurrentScope().getUI().sendMessage(coreBundle.getString("no.validation.errors.found"));
        }
    }

    private ValidateChangeLogIterator getValidateChangelogIterator(DatabaseChangeLog changeLog) {
        List<ValidatorFilter> validatorFilters = this.getValidateChangelogIteratorFilters();
        return new ValidateChangeLogIterator(changeLog, validatorFilters.toArray(new ValidatorFilter[0]));
    }

    private List<ValidatorFilter> getValidateChangelogIteratorFilters() {
        List<ValidatorFilter> filters = new ArrayList<>();
        filters.add(new RequiredFieldsValidatorFilter());
        filters.add(new DbmsValidatorFilter());
        filters.add(new RunWithValidatorFilter());
        filters.add(new PreconditionsValidatorFilter());
        filters.add(new LabelsValidatorFilter());
        filters.add(new ContextValidatorFilter());
        filters.add(new LogicalFilePathValidatorFilter());
        return filters;
    }
}
