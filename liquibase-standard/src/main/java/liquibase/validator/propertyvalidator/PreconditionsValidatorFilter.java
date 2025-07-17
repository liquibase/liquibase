package liquibase.validator.propertyvalidator;

import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.validator.RawChangeSet;
import liquibase.changelog.filter.ChangeSetFilter;
import liquibase.changelog.filter.ChangeSetFilterResult;
import liquibase.precondition.Precondition;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Validates the preconditions of a {@link RawChangeSet}. Ensures that the preconditions are valid based on the changeLog format and strict mode.
 * If the preconditions are invalid, it won't accept the provided changeSet and will provide a validation error message.
 */
public class PreconditionsValidatorFilter implements ValidatorFilter {

    private final List<String> availablePreconditions;

    public PreconditionsValidatorFilter() {
        this.availablePreconditions = getAvailablePreconditions();
    }

    public static List<String> getAvailablePreconditions() {
        return Scope.getCurrentScope().getServiceLocator().findInstances(Precondition.class).stream()
                .map(Precondition::getName)
                .collect(Collectors.toList());
    }

    @Override
    public ChangeSetFilterResult accepts(RawChangeSet changeSet) {
        StringBuilder validationErrors = new StringBuilder();

        validateParsedPreconditions(changeSet, validationErrors);


        if (validationErrors.length() > 0) {
            return new ChangeSetFilterResult(false, validationErrors.toString(),
                    ChangeSetFilter.class, "invalidPreconditionsForSQLType", "preconditions");
        } else {
            return new ChangeSetFilterResult(true, "Valid preconditions",
                    ChangeSetFilter.class, "validPreconditions", "preconditions");
        }
    }

    /**
     * Validates the preconditions parsed from the changeSet. This method will check if preconditions are valid based on the generated list of available preconditions
     * that Liquibase provides. For SQL changeLog format, it checks if the preconditions are limited to a specific set of valid preconditions.
     *
     * @param changeSet The RawChangeSet to validate.
     * @param validationErrors StringBuilder to accumulate validation error messages.
     */
    private void validateParsedPreconditions(RawChangeSet changeSet, StringBuilder validationErrors) {
        List<String> preconditionNames = changeSet.getPreconditions();
        boolean strict = GlobalConfiguration.STRICT.getCurrentValue();

        if (strict) {
            if (changeSet.getChangeLogFormat() != null && changeSet.getChangeLogFormat().equals("sql")) {
                if (!preconditionNames.isEmpty()) {
                    String preconditionName = preconditionNames.get(0);
                    if (!(preconditionName.equals("table-exists") ||
                            preconditionName.equals("view-exists") ||
                            preconditionName.equals("sql-check") ||
                            preconditionName.equals("sql"))) {

                        validationErrors.append(String.format("%n\t- %s is not a valid precondition for SQL changeLog format",
                                preconditionName));
                    }
                }
            }
            else {
                for (String preconditionName : preconditionNames) {
                    if (!availablePreconditions.contains(preconditionName)) {
                        validationErrors.append(String.format("%n\t- %s is not a valid precondition", preconditionName));
                    }
                }
            }
        }
    }
}
