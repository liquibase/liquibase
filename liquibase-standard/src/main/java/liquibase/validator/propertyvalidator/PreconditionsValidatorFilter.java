package liquibase.validator.propertyvalidator;

import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.validator.RawChangeSet;
import liquibase.changelog.filter.ChangeSetFilter;
import liquibase.changelog.filter.ChangeSetFilterResult;
import liquibase.precondition.Precondition;

import java.util.List;
import java.util.stream.Collectors;

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
