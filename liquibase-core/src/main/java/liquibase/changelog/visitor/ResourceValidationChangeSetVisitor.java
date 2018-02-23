package liquibase.changelog.visitor;

import liquibase.change.Change;
import liquibase.change.ResourceDependentChange;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.filter.ChangeSetFilterResult;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import liquibase.exception.ValidationErrors;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class ResourceValidationChangeSetVisitor implements ChangeSetVisitor {

    private final ValidationErrors validationErrors;

    public ResourceValidationChangeSetVisitor() {
        validationErrors = new ValidationErrors();
    }

    @Override
    public Direction getDirection() {
        return ChangeSetVisitor.Direction.FORWARD;
    }

    @Override
    public void visit(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database, Set<ChangeSetFilterResult> filterResults) throws LiquibaseException {
        validateChanges(changeSet.getChanges());
        validateChanges(changeSet.getRollback().getChanges());

    }

    private void validateChanges(List<Change> changes) {
        for (Change change : changes) {
            if (change instanceof ResourceDependentChange) {
                ResourceDependentChange resourceDependentChange = (ResourceDependentChange) change;
                try {
                    resourceDependentChange.openSqlStream();
                } catch (IOException e) {
                    validationErrors.addError(e.getMessage());
                }
            }
        }
    }

    public ValidationErrors getValidationErrors() {
        return validationErrors;
    }
}
