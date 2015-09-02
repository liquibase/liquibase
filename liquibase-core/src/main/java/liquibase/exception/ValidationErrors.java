package liquibase.exception;

import liquibase.ExtensibleObject;
import liquibase.action.Action;
import liquibase.action.core.AddPrimaryKeyAction;
import liquibase.action.core.SetNullableAction;
import liquibase.database.Database;
import liquibase.changelog.ChangeSet;
import liquibase.structure.ObjectName;
import liquibase.util.StringUtils;

import java.util.*;

public class ValidationErrors {

    protected List<String> errorMessages = new ArrayList<String>();
    protected List<String> warningMessages = new ArrayList<String>();

    public boolean hasErrors() {
        return errorMessages.size() > 0;
    }

    public boolean hasWarnings() {
        return warningMessages.size() > 0;
    }

    /**
     * More convenient version of {@link #checkRequiredField(String, Object)} that doesn't require you to lookup the value first.
     * Instead, simply pass the field and the base object.
     */
    public ValidationErrors checkForRequiredField(String requiredFieldName, ExtensibleObject object) {
        Object value = object.get(requiredFieldName, Object.class);
        checkRequiredField(requiredFieldName, value);

        return this;
    }

    public ValidationErrors checkForRequiredField(Enum requiredFieldName, ExtensibleObject object) {
        return checkForRequiredField(requiredFieldName.name(), object);
    }

    /**
     * Less convenient version of {@link #checkForRequiredField(Enum, liquibase.ExtensibleObject)} that takes a value to check if it is null or empty and the field for the error message.
     * This method will eventually be removed.
     */
    public void checkRequiredField(String requiredFieldName, Object value) {
        if (value == null) {
            addError(requiredFieldName + " is required");
        } else if (value instanceof Collection && ((Collection) value).size() == 0) {
            addError(requiredFieldName + " is empty");
        } else if (value instanceof Object[] && ((Object[]) value).length == 0) {
            addError(requiredFieldName + " is empty");
        }
    }

    /**
     * If an error was added that the given field is required, remove the error.
     */
    public ValidationErrors removeRequiredField(String field) {
        ListIterator<String> it = errorMessages.listIterator();
        while (it.hasNext()) {
            String message = it.next();
            if (message.equals(field+" is required") || message.equals(field+" is empty")) {
                it.remove();
            }
        }
        return this;
    }

    public ValidationErrors removeRequiredField(Enum field) {
        return removeRequiredField(field.name());

    }

    public ValidationErrors addUnsupportedError(String message, String scopeDescription) {
        addError(message + " is not supported in " + scopeDescription);
        return this;
    }

    /**
     * More convenient version of {@link #checkDisallowedField(String, Object, String)} that doesn't require you to lookup the value first.
     * Instead, simply pass the field and the base object.
     */
    public ValidationErrors checkForDisallowedField(String disallowedField, ExtensibleObject object, String scopeDescription) {
        if (object != null) {
            Object value = object.get(disallowedField, Object.class);
            checkDisallowedField(disallowedField, value, scopeDescription);
        }
        return this;
    }

    public ValidationErrors checkForDisallowedField(Enum disallowedField, ExtensibleObject object, String scopeDescription) {
        return checkForDisallowedField(disallowedField.name(), object, scopeDescription);
    }

    public void checkDisallowedField(String disallowedFieldName, Object value, String scopeDescription) {
        if (value != null) {
            addError(disallowedFieldName + " is not allowed in "+scopeDescription);
        }
    }

    public ValidationErrors addError(String message) {
        errorMessages.add(message);
        return this;
    }

    public ValidationErrors addWarning(String message) {
        warningMessages.add(message);
        return this;
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }

    public List<String> getWarningMessages() {
        return warningMessages;
    }

    public ValidationErrors addAll(ValidationErrors validationErrors) {
        if (validationErrors == null) {
            return this;
        }
        this.errorMessages.addAll(validationErrors.getErrorMessages());
        this.warningMessages.addAll(validationErrors.getWarningMessages());
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
        String string;
        if (!hasErrors()) {
            string = "No errors";
        } else {
            string = StringUtils.join(getErrorMessages(), "; ");
        }

        if (hasWarnings()) {
            string = StringUtils.join(getWarningMessages(), "; ", new StringUtils.StringUtilsFormatter() {
                @Override
                public String toString(Object obj) {
                    return "WARNING: "+obj;
                }
            });
        }

        return string;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    public List<String> getRequiredErrorMessages() {
        List<String> requiredErrorMessages = new ArrayList<String>();
        for (String message : errorMessages) {
            if (message.contains("is required")) {
                requiredErrorMessages.add(message);
            }
        }
        return Collections.unmodifiableList(requiredErrorMessages);
    }

    public List<String> getUnsupportedErrorMessages() {
        List<String> unsupportedErrorMessages = new ArrayList<String>();
        for (String message : errorMessages) {
            if (message.contains(" is not allowed on ") || message.contains(" is not supported in ")) {
                unsupportedErrorMessages.add(message);
            }
        }
        return Collections.unmodifiableList(unsupportedErrorMessages);
    }

    public ValidationErrors removeUnsupportedField(Enum field) {
        return this;
    }

    public ValidationErrors removeUnsupportedField(String field) {
        return this;
    }

    public ValidationErrors check(String errorMessage, ErrorCheck check) {
        if (!hasErrors()) {
            if (!check.check()) {
                addError(errorMessage);
            }
        }
        return this;
    }

    /**
     * Convenience method for {@link #checkForRequiredContainer(String, String, Action)}
     */
    public ValidationErrors checkForRequiredContainer(String errorMessage, Enum field, Action action) {
        return checkForRequiredContainer(errorMessage, field.name(), action);
    }

    /**
     * Adds the given errorMessage to the validationErrors if the {@link ObjectName} on the given field is set but doesn't have a container defined.
     * If the given field is not set, no error message is set.
     */
    public ValidationErrors checkForRequiredContainer(String errorMessage, String field, Action action) {
        ObjectName objectName = action.get(field, ObjectName.class);
        if (objectName != null) {
            if (objectName.container == null) {
                addError(errorMessage);
            } else if (objectName.container.name == null) {
                addError(errorMessage);
            }
        }
        return this;
    }

    public interface ErrorCheck {

        boolean check();
    }
}
