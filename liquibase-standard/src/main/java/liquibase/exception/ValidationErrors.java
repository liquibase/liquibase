package liquibase.exception;

import liquibase.Scope;
import liquibase.change.Change;
import liquibase.change.ChangeFactory;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.precondition.Precondition;
import liquibase.util.StringUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ValidationErrors {

    protected List<String> errorMessages = new ArrayList<>();
    protected List<String> warningMessages = new ArrayList<>();
    protected String change = null;

    public boolean hasErrors() {
        return !errorMessages.isEmpty();
    }

    public ValidationErrors() {
    }

    public ValidationErrors(String change) {
        this.change = change;
    }

    public ValidationErrors(Change change) {
        this.change = Scope.getCurrentScope().getSingleton(ChangeFactory.class).getChangeMetaData(change).getName();
    }

    public ValidationErrors(Precondition precondition) {
        this.change = precondition.getName();
    }

    public String getChangeName() {
        return this.change;
    }

    /**
     * Convenience method for {@link #checkRequiredField(String, Object, String, boolean)} with allowEmptyValue=false
     */
    public ValidationErrors checkRequiredField(String requiredFieldName, Object value) {
        return checkRequiredField(requiredFieldName, value, false);
    }

    /**
     * Convenience method for {@link #checkRequiredField(String, Object, String, boolean)} with a null postfix
     */
    public ValidationErrors checkRequiredField(String requiredFieldName, Object value, boolean allowEmptyValue) {
        return checkRequiredField(requiredFieldName, value, null, allowEmptyValue);
    }

    /**
     * Convenience method for {@link #checkRequiredField(String, Object, String, boolean)} with allowEmptyValue=false
     */
    public ValidationErrors checkRequiredField(String requiredFieldName, Object value, String postfix) {
        return checkRequiredField(requiredFieldName, value, postfix, false);
    }

    /**
     * Checks that the given value is set.
     * @param allowEmptyValue  If true, empty string and empty arrays are allowed. If false, they are not.
     */
    public ValidationErrors checkRequiredField(String requiredFieldName, Object value, String postfix, boolean allowEmptyValue) {
        String err = null;
        if (value == null) {
            err = requiredFieldName + " is required";
        }

        if (!allowEmptyValue) {
            if ((value instanceof Collection && ((Collection<?>) value).isEmpty())
                    || (value instanceof Object[] && ((Object[]) value).length == 0)) {
                err = "No " + requiredFieldName + " defined";
            } else if (value instanceof String && StringUtil.trimToNull((String) value) == null) {
                err = requiredFieldName + " is empty";
            }
        }

        if (err != null) {
            addError(err + (this.change == null ? "" : " for " + this.change)
                    + (postfix == null ? "" : postfix));
        }
        return this;
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
            addError(disallowedFieldName + " is not allowed on " + (database == null ? "unknown" : database.getShortName()));
        }
    }

    public ValidationErrors addError(String message) {
        errorMessages.add(message);
        return this;
    }


    public ValidationErrors addError(String message, ChangeSet changeSet) {
        this.errorMessages.add(message + ", " + changeSet);
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
            this.addError(message, changeSet);
        }
        for (String message : validationErrors.getWarningMessages()) {
            this.warningMessages.add(message + ", " + changeSet);
        }
    }

    @Override
    public String toString() {
        if (getErrorMessages().isEmpty()) {
            return "No errors";
        }
        return StringUtil.join(getErrorMessages(), "; ");
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ValidationErrors)) {
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
