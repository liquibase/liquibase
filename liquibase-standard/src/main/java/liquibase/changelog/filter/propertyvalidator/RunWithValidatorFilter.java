package liquibase.changelog.filter.propertyvalidator;

import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.changelog.RawChangeSet;
import liquibase.changelog.filter.ChangeSetFilter;
import liquibase.changelog.filter.ChangeSetFilterResult;
import liquibase.executor.ExecutorService;

import java.util.List;

public class RunWithValidatorFilter implements ValidatorFilter {
    private final List<String> validRunWithOptions;

    public RunWithValidatorFilter() {
        this.validRunWithOptions = Scope.getCurrentScope().getSingleton(ExecutorService.class).getAllExecutorNames();
    }

    private boolean isValidRunWithOption(String runWith) {
        return this.validRunWithOptions.stream().anyMatch(runWithOption -> runWithOption.equalsIgnoreCase(runWith));
    }

    @Override
    public ChangeSetFilterResult accepts(RawChangeSet changeSet) {
        boolean strict = GlobalConfiguration.STRICT.getCurrentValue();
        StringBuilder errors = new StringBuilder();

        if(strict){
            String[] changeSetRunWithOptions = changeSet.getRunWith() != null ? changeSet.getRunWith().split(",") : new String[]{ changeSet.getRunWith() };

            for (String runWithOption : changeSetRunWithOptions) {
                if(runWithOption!= null){
                    if((runWithOption.trim().isEmpty())) {
                        errors.append(String.format("%n\t- runWith value cannot be empty while on Strict mode"));
                    }
                    if((!runWithOption.trim().isEmpty()) && !isValidRunWithOption(runWithOption.trim())) {
                        errors.append(String.format("%n\t- %s is not a valid runWith value", runWithOption));
                    }
                }
            }
        }

        if(errors.length() == 0) {
            return new ChangeSetFilterResult(true, "Valid runWith values: " + String.join(", ", validRunWithOptions), ChangeSetFilter.class, "validRunWith", "runWith");
        } else {
            return new ChangeSetFilterResult(false, errors.toString(), ChangeSetFilter.class, "invalidRunWith", "runWith");
        }
    }
}
