package liquibase.exception;

public class ResourceValidationFailedException extends LiquibaseException {

    private ValidationErrors validationErrors;

    public ResourceValidationFailedException(ValidationErrors validationErrors) {
        this.validationErrors = validationErrors;
    }

    @Override
    public String getMessage() {
        return validationErrors.toString();
    }
}
