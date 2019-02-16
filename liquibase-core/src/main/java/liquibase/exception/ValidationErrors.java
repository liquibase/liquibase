package liquibase.exception;

import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ValidationErrors {

    protected List<String> errorMessages = new ArrayList<>();
    protected List<String> warningMessages = new ArrayList<>();

    public boolean hasErrors() {
        return !errorMessages.isEmpty();
    }

    public void checkRequiredField(String requiredFieldName, Object value) {
        if (value == null) {
            addError(requiredFieldName + " is required");
        } else if ((value instanceof Collection) && ((Collection) value).isEmpty()) {
            addError(requiredFieldName + " is empty");
        } else if ((value instanceof Object[]) && (((Object[]) value).length == 0)) {
            addError(requiredFieldName + " is empty");
        }
    }

    /**
     * <p>Checks if a field is forbidden in combination with a given Database (most often because that database
     * does not implement the features required by the field/value combination). If a "forbidden" use is detected,
     * a validation error is added to the current list of ValidationErrors.</p>
     * Note:
     * <ul>
     * <li>if value==null, the field is ALLOWED for all DBs</li>
     * <li>if the disallowedDatabases list does not at least contain 1 entry, the field is NOT allowed</li>
     * </ul>
     *
     * @param disallowedFieldName field whose value is checked
     * @param value               value that might be disallowed
     * @param database            database the object/value combination is checked against
     * @param disallowedDatabases a list of "forbidden" databases that do not allow this field/value combination
     */
    @SafeVarargs
    public final void checkDisallowedField(String disallowedFieldName, Object value, Database database,
                                           Class<? extends Database>... disallowedDatabases) {
        boolean isDisallowed = false;
        if ((disallowedDatabases == null) || (disallowedDatabases.length == 0)) {
            isDisallowed = true;
        } else {
            for (Class<? extends Database> databaseClass : disallowedDatabases) {
                if (databaseClass.isAssignableFrom(database.getClass())) {
                    isDisallowed = true;
                }
            }
        }

        if (isDisallowed && (value != null)) {
            addError(disallowedFieldName + " is not allowed on "+(database == null?"unknown":database.getShortName()));
        }
    }

    public ValidationErrors addError(String message) {
        errorMessages.add(message);
        return this;
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }

    public ValidationErrors addWarning(String message) {
        warningMessages.add(message);
        return this;
    }

    public List<String> getWarningMessages() {
        return warningMessages;
    }

    public ValidationErrors addAll(ValidationErrors validationErrors) {
        if (validationErrors == null) {
            return this;
        }
        this.errorMessages.addAll(validationErrors.getErrorMessages());
        return this;
    }

    public void addAll(ValidationErrors validationErrors, ChangeSet changeSet) {
        for (String message : validationErrors.getErrorMessages()) {
            this.errorMessages.add(message+", "+changeSet);
        }
        for (String message : validationErrors.getWarningMessages()) {
            this.warningMessages.add(message+", "+changeSet);
        }
    }

    @Override
    public String toString() {
        if (getErrorMessages().isEmpty()) {
            return "No errors";
        }
        return StringUtils.join(getErrorMessages(), "; ");
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if ((obj == null) || !(obj instanceof ValidationErrors)) {
            return false;
        }
        return this.toString().equals(obj.toString());
    }

    public List<String> getRequiredErrorMessages() {
        List<String> requiredErrorMessages = new ArrayList<>();
        for (String message : errorMessages) {
            if (message.contains("is required")) {
                requiredErrorMessages.add(message);
            }
        }
        return Collections.unmodifiableList(requiredErrorMessages);
    }

    public List<String> getUnsupportedErrorMessages() {
        List<String> unsupportedErrorMessages = new ArrayList<>();
        for (String message : errorMessages) {
            if (message.contains(" is not allowed on ")) {
                unsupportedErrorMessages.add(message);
            }
        }
        return Collections.unmodifiableList(unsupportedErrorMessages);
    }
}
