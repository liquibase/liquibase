package liquibase.exception;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

public class ValidationErrors {

    protected List<String> errorMessages = new ArrayList<String>();

    public boolean hasErrors() {
        return errorMessages.size() > 0;
    }

    public void checkRequiredField(String requiredFieldName, Object value) {
        if (value == null) {
            addError(requiredFieldName+" is required");
        } else {
            if (value instanceof Collection && ((Collection) value).size() == 0) {
                addError(requiredFieldName+" is empty");
            }
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

    public void addAll(ValidationErrors validationErrors) {
        this.errorMessages.addAll(validationErrors.getErrorMessages());
    }
}
