package liquibase.statement.generator;

import java.util.ArrayList;
import java.util.List;

public class GeneratorValidationErrors {
    private List<String> errorMessages;

    public GeneratorValidationErrors() {
        this.errorMessages = new ArrayList<String>();
    }

    public boolean hasErrors() {
        return errorMessages.size() > 0;
    }

    public void checkRequiredField(String requiredFieldName, Object value) {
        if (value == null) {
            addError(requiredFieldName+" is required");
        }
    }

    public void checkDisallowedField(String disallowedFieldName, Object value) {
        if (value != null) {
            addError(disallowedFieldName+" is not allowed");
        }
    }

    public void addError(String message) {
        errorMessages.add(message);
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }

}
