package liquibase.exception;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.visitor.ValidatingVisitor;
import liquibase.database.Database;
import liquibase.logging.LogFactory;
import liquibase.precondition.ErrorPrecondition;
import liquibase.precondition.FailedPrecondition;
import liquibase.util.StreamUtil;

import java.io.PrintStream;
import java.util.List;
import java.util.Set;

public class ValidationFailedException extends MigrationFailedException {

    private List<ChangeSet> invalidMD5Sums;
    private List<FailedPrecondition> failedPreconditions;
    private List<ErrorPrecondition> errorPreconditions;
    private Set<ChangeSet> duplicateChangeSets;
    private List<SetupException> setupExceptions;
    private List<Throwable> changeValidationExceptions;
    private ValidationErrors validationErrors;
    private Database database;

    public ValidationFailedException(ValidatingVisitor changeLogHandler) {
        this.invalidMD5Sums = changeLogHandler.getInvalidMD5Sums();
        this.failedPreconditions = changeLogHandler.getFailedPreconditions();
        this.errorPreconditions = changeLogHandler.getErrorPreconditions();
        this.duplicateChangeSets = changeLogHandler.getDuplicateChangeSets();
        this.setupExceptions = changeLogHandler.getSetupExceptions();
        this.changeValidationExceptions = changeLogHandler.getChangeValidationExceptions();
        this.validationErrors = changeLogHandler.getValidationErrors();
        this.database = changeLogHandler.getDatabase();
    }


    @Override
    public String getMessage() {
        StringBuffer message = new StringBuffer();
        message.append("Validation Failed:").append(StreamUtil.getLineSeparator());
        if (invalidMD5Sums.size() > 0) {
            message.append("     ").append(invalidMD5Sums.size()).append(" change sets check sum").append(StreamUtil.getLineSeparator());
            for (int i=0; i< invalidMD5Sums.size(); i++) {
                if (i > 25) {
                    break;
                }
                ChangeSet invalid = invalidMD5Sums.get(i);

                message.append("          ").append(invalid.toString(false)).append(" is now: ").append(invalid.generateCheckSum());
                message.append(StreamUtil.getLineSeparator());
            }
        }
        if (failedPreconditions.size() > 0) {
            message.append("     ").append(failedPreconditions.size()).append(" preconditions failed").append(StreamUtil.getLineSeparator());
            for (FailedPrecondition invalid : failedPreconditions) {
                message.append("     ").append(invalid.toString());
                message.append(StreamUtil.getLineSeparator());
            }
        }
        if (errorPreconditions.size() > 0) {
            message.append("     ").append(errorPreconditions.size()).append(" preconditions generated an error").append(StreamUtil.getLineSeparator());
            for (ErrorPrecondition invalid : errorPreconditions) {
                message.append("     ").append(invalid.toString());
                message.append(StreamUtil.getLineSeparator());
            }
        }
        if (duplicateChangeSets.size() > 0) {
            message.append("     ").append(duplicateChangeSets.size()).append(" change sets had duplicate identifiers").append(StreamUtil.getLineSeparator());
            for (ChangeSet invalid : duplicateChangeSets) {
                message.append("          ").append(invalid.toString(false));
                message.append(StreamUtil.getLineSeparator());
            }
        }
        if(setupExceptions.size() >0){
            message.append("     ").append(setupExceptions.size()).append(" changes have failures").append(StreamUtil.getLineSeparator());
            for (SetupException invalid : setupExceptions) {
                message.append("          ").append(invalid.toString());
                message.append(StreamUtil.getLineSeparator());                
            }
        }
        if(changeValidationExceptions.size() >0){
            message.append("     ").append(changeValidationExceptions.size()).append(" changes have validation errors").append(StreamUtil.getLineSeparator());
            for (Throwable invalid : changeValidationExceptions) {
                LogFactory.getLogger().debug("validation exception", invalid);
                message.append("          ").append(invalid.toString());
                message.append(StreamUtil.getLineSeparator());
            }
        }
        if(validationErrors.hasErrors()){
            message.append("     ").append(validationErrors.getErrorMessages().size()).append(" changes have validation failures").append(StreamUtil.getLineSeparator());
            for (String invalid : validationErrors.getErrorMessages()) {
                message.append("          ").append(invalid);
                message.append(StreamUtil.getLineSeparator());
            }
        }

        return message.toString();
    }

    public List<ChangeSet> getInvalidMD5Sums() {
        return invalidMD5Sums;
    }

    public void printDescriptiveError(PrintStream out) {
        out.println("Validation Error: ");
        if (invalidMD5Sums.size() > 0) {
            out.println("     "+invalidMD5Sums.size()+" change sets have changed since they were ran against the database");
            for (ChangeSet changeSet : invalidMD5Sums) {
                out.println("          "+changeSet.toString(false));
            }
        }

        if (failedPreconditions.size() > 0) {
            out.println("     "+failedPreconditions.size()+" preconditions failed");
            for (FailedPrecondition failedPrecondition : failedPreconditions) {
                out.println("          "+failedPrecondition.toString());
            }
        }
        if (errorPreconditions.size() > 0) {
            out.println("     "+errorPreconditions.size()+" preconditions generated an error");
            for (ErrorPrecondition errorPrecondition : errorPreconditions) {
                out.println("          "+errorPrecondition.toString());
            }
        }

        if (duplicateChangeSets.size() > 0) {
            out.println("     "+duplicateChangeSets.size()+" change sets had duplicate identifiers");
            for (ChangeSet duplicate : duplicateChangeSets) {
                out.println("          "+duplicate.toString(false));
            }
        }
        
        if(setupExceptions.size() >0) {
            out.println("     "+setupExceptions.size()+" changes had errors");
            for (SetupException setupEx : setupExceptions) {
                out.println("          "+setupEx.getMessage());
            }
        }
    }
}
