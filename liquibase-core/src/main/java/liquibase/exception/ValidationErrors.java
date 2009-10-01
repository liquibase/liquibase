package liquibase.exception;

import liquibase.database.Database;
import liquibase.changelog.ChangeSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ValidationErrors {

    protected List<String> errorMessages = new ArrayList<String>();

    public boolean hasErrors() {
        return errorMessages.size() > 0;
    }

    public void checkRequiredField(String requiredFieldName, Object value) {
        if (value == null) {
            addError(requiredFieldName + " is required");
        } else if (value instanceof Collection && ((Collection) value).size() == 0) {
            addError(requiredFieldName + " is empty");
        } else if (value instanceof Object[] && ((Object[]) value).length == 0) {
            addError(requiredFieldName + " is empty");
        }
    }

    public void checkDisallowedField(String disallowedFieldName, Object value, Database database, Class<? extends Database>... disallowedDatabases) {
        boolean isDisallowed = false;
        if (disallowedDatabases == null || disallowedDatabases.length == 0) {
            isDisallowed = true;
        } else {
            for (Class<? extends Database> databaseClass : disallowedDatabases) {
                if (databaseClass.isAssignableFrom(database.getClass())) {
                    isDisallowed = true;
                }
            }
        }

        if (isDisallowed && value != null) {
            addError(disallowedFieldName + " is not allowed on "+database.getTypeName());
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

    public void addAll(ValidationErrors validationErrors, ChangeSet changeSet) {
        for (String message : validationErrors.getErrorMessages()) {
            this.errorMessages.add(message+", "+changeSet);
        }
    }
}
