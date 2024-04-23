package liquibase.changelog.visitor;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.RanChangeSet;
import liquibase.database.Database;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.plugin.Plugin;
import liquibase.precondition.ErrorPrecondition;
import liquibase.precondition.FailedPrecondition;

import java.util.List;
import java.util.Set;

/**
 * The ValidatingVisitor interface allows implementations to supply their own version of a ValidatingVisitor. By default
 * Liquibase uses the {@link StandardValidatingVisitor}. To use your own, you must register it with a higher priority
 * in the {@link ValidatingVisitorFactory}.
 */
public interface ValidatingVisitor extends Plugin, ChangeSetVisitor {

    int getPriority();

    void validate(Database database, DatabaseChangeLog databaseChangeLog);

    boolean validationPassed();

    List<String> getInvalidMD5Sums();

    String getFailedPreconditionsMessage();

    List<FailedPrecondition> getFailedPreconditions();

    String getErrorPreconditionsMessage();

    List<ErrorPrecondition> getErrorPreconditions();

    Set<ChangeSet> getDuplicateChangeSets();

    List<SetupException> getSetupExceptions();

    List<Throwable> getChangeValidationExceptions();

    ValidationErrors getValidationErrors();

    Warnings getWarnings();

    void setRanChangeSetList(List<RanChangeSet> ranChangeSetList);
}
