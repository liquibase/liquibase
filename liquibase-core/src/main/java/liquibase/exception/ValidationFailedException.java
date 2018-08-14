package liquibase.exception;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.visitor.ValidatingVisitor;
import liquibase.logging.LogService;
import liquibase.logging.LogType;
import liquibase.precondition.ErrorPrecondition;
import liquibase.precondition.FailedPrecondition;
import liquibase.util.StreamUtil;

import java.io.PrintStream;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import static java.util.ResourceBundle.getBundle;

public class ValidationFailedException extends MigrationFailedException {
    
    private static final long serialVersionUID = -6824856974397660436L;
    public static final String INDENT_SPACES = "     ";
    private static ResourceBundle coreBundle = getBundle("liquibase/i18n/liquibase-core");
    private List<String> invalidMD5Sums;
    private List<FailedPrecondition> failedPreconditions;
    private List<ErrorPrecondition> errorPreconditions;
    private Set<ChangeSet> duplicateChangeSets;
    private List<SetupException> setupExceptions;
    private List<Throwable> changeValidationExceptions;
    private ValidationErrors validationErrors;

    public ValidationFailedException(ValidatingVisitor changeLogHandler) {
        this.invalidMD5Sums = changeLogHandler.getInvalidMD5Sums();
        this.failedPreconditions = changeLogHandler.getFailedPreconditions();
        this.errorPreconditions = changeLogHandler.getErrorPreconditions();
        this.duplicateChangeSets = changeLogHandler.getDuplicateChangeSets();
        this.setupExceptions = changeLogHandler.getSetupExceptions();
        this.changeValidationExceptions = changeLogHandler.getChangeValidationExceptions();
        this.validationErrors = changeLogHandler.getValidationErrors();
    }

    @Override
    public String getMessage() {
        StringBuilder message = new StringBuilder();
        String separator = StreamUtil.getLineSeparator();
        message.append(coreBundle.getString("validation.failed")).append(separator);
        
        if (!invalidMD5Sums.isEmpty()) {
            message.append(INDENT_SPACES).append(
                String.format(coreBundle.getString("check.sum.changed"), invalidMD5Sums.size())).append(separator);
            for (int i=0; i< invalidMD5Sums.size(); i++) {
                if (i > 25) {
                    break;
                }
                message.append("          ").append(invalidMD5Sums.get(i));
                message.append(separator);
            }
        }
        
        if (!failedPreconditions.isEmpty()) {
            message.append(INDENT_SPACES).append(
                String.format(coreBundle.getString("preconditions.failed"), failedPreconditions.size()))
                .append(separator);
            for (FailedPrecondition invalid : failedPreconditions) {
                message.append(INDENT_SPACES).append(invalid.toString());
                message.append(separator);
            }
        }
        
        if (!errorPreconditions.isEmpty()) {
            message.append(INDENT_SPACES).append(String.format(coreBundle.getString(
                "preconditions.generated.error"), errorPreconditions.size()))
                .append(separator);
            for (ErrorPrecondition invalid : errorPreconditions) {
                message.append(INDENT_SPACES).append(invalid.toString());
                message.append(separator);
            }
        }
        
        if (!duplicateChangeSets.isEmpty()) {
            message.append(INDENT_SPACES).append(String.format(
                coreBundle.getString("change.sets.duplicate.identifiers"),
                duplicateChangeSets.size())).append(separator);
            for (ChangeSet invalid : duplicateChangeSets) {
                message.append("          ").append(invalid.toString(false));
                message.append(separator);
            }
        }
        
        if(!setupExceptions.isEmpty()){
            message.append(INDENT_SPACES).append(
                String.format(coreBundle.getString("changes.have.failures"), setupExceptions.size())).append(separator);
            for (SetupException invalid : setupExceptions) {
                message.append("          ").append(invalid.toString());
                message.append(separator);                
            }
        }
        
        if(!changeValidationExceptions.isEmpty()){
            message.append(INDENT_SPACES)
                .append(String.format(
                    coreBundle.getString("changes.have.validation.errors"), changeValidationExceptions.size())
                ).append(separator);
            for (Throwable invalid : changeValidationExceptions) {
                LogService.getLog(getClass()).debug(LogType.LOG, coreBundle.getString("validation.exception"), invalid);
                message.append("          ").append(invalid.toString());
                message.append(separator);
            }
        }
        if(validationErrors.hasErrors()){
            message.append(INDENT_SPACES).append(String.format(
                coreBundle.getString("changes.have.validation.failures"),
                validationErrors.getErrorMessages().size()
                )).append(separator);
            for (String invalid : validationErrors.getErrorMessages()) {
                message.append("          ").append(invalid);
                message.append(separator);
            }
        }

        return message.toString();
    }

    public List<String> getInvalidMD5Sums() {
        return invalidMD5Sums;
    }

    public void printDescriptiveError(PrintStream out) {
        out.println("Validation Error: ");
        if (!invalidMD5Sums.isEmpty()) {
            out.println(INDENT_SPACES +invalidMD5Sums.size()+" change sets have changed since they were ran against the database");
            for (String message : invalidMD5Sums) {
                out.println("          " + message);
            }
        }

        if (!failedPreconditions.isEmpty()) {
            out.println(INDENT_SPACES +failedPreconditions.size()+" preconditions failed");
            for (FailedPrecondition failedPrecondition : failedPreconditions) {
                out.println("          "+failedPrecondition.toString());
            }
        }
        if (!errorPreconditions.isEmpty()) {
            out.println(INDENT_SPACES +errorPreconditions.size()+" preconditions generated an error");
            for (ErrorPrecondition errorPrecondition : errorPreconditions) {
                out.println("          "+errorPrecondition.toString());
            }
        }

        if (!duplicateChangeSets.isEmpty()) {
            out.println(INDENT_SPACES +duplicateChangeSets.size()+" change sets had duplicate identifiers");
            for (ChangeSet duplicate : duplicateChangeSets) {
                out.println("          "+duplicate.toString(false));
            }
        }
        
        if(!setupExceptions.isEmpty()) {
            out.println(INDENT_SPACES +setupExceptions.size()+" changes had errors");
            for (SetupException setupEx : setupExceptions) {
                out.println("          "+setupEx.getMessage());
            }
        }
    }
}
