package liquibase.action;

import liquibase.ExtensibleObject;
import liquibase.Scope;
import liquibase.util.ObjectUtil;
import liquibase.util.StringUtils;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Returned by {@link Action#checkStatus(Scope)} to describe if the action has already been applied.
 * Possible values:
 * <ul>
 * <li>Complete: The action has been fully completed</li>
 * <li>Incorrect: The action was executed, but the current state doesn't quite match. For example, a CreateTableAction was ran, but some of the column definitions don't match</li>
 * <li>Not Applied: The action was not applied</li>
 * <li>Unknown: The current state cannot be checked</li>
 * <li>Cannot Verify: The action cannot be verified</li>
 * </ul>
 */
public class ActionStatus {

    protected Throwable exception;

    private Map<Status, SortedSet<String>> messages = new HashMap<>();
    private boolean atLeastOneAssertion = false;

    public ActionStatus() {
        messages.put(Status.cannotVerify, new TreeSet<String>());
        messages.put(Status.unknown, new TreeSet<String>());
        messages.put(Status.incorrect, new TreeSet<String>());
        messages.put(Status.notApplied, new TreeSet<String>());
    }

    /**
     * Adds the given incompleteMessage to this status if the complete flag is false.
     */
    public ActionStatus assertApplied(boolean complete, String incompleteMessage) {
        if (!complete) {
            messages.get(Status.notApplied).add(ObjectUtil.defaultIfEmpty(incompleteMessage, "No message"));
        }
        atLeastOneAssertion = true;
        return this;
    }

    /**
     * Convenience method like {@link #assertCorrect(ExtensibleObject, ExtensibleObject, String)} but tests all properties
     */
    public <T extends ExtensibleObject> ActionStatus assertCorrect(T correctObject, T objectToCheck) {
        for (String property : correctObject.getAttributeNames()) {
            assertCorrect(correctObject, objectToCheck, property);
        }

        return this;

    }

    /**
     * Convenience method for {@link #assertCorrect(boolean, String)} when you are comparing properties that are the same across two objects.
     * If the value is defined in the correctObject, it must have the same value in objectToCheck. If the object is null in correctObject, it doesn't matter what the value is in the objectToCheck.
     *
     * Does not support and therefore does not check Collection values.
     */
    public ActionStatus assertCorrect(ExtensibleObject correctObject, ExtensibleObject objectToCheck, String propertyName) {
        Object correctValue = correctObject.get(propertyName, Object.class);
        Object checkValue = objectToCheck.get(propertyName, Object.class);

        if (correctValue instanceof Collection) {
            return this;
        }

        boolean correct = correctValue == null || correctValue.equals(checkValue);

        return assertCorrect(correct, "'" + propertyName + "' is incorrect (expected '" + correctValue + "' got '" + checkValue + "')");
    }

    /**
     * Adds the given incorrectMessage to this status if the correct flag is false.
     */
    public ActionStatus assertCorrect(boolean correct, String incorrectMessage) {
        if (!correct) {
            messages.get(Status.incorrect).add(ObjectUtil.defaultIfEmpty(incorrectMessage, "No message"));
        }

        atLeastOneAssertion = true;
        return this;
    }


    /**
     * Marks this status as unknown because of the given message.
     */
    public ActionStatus unknown(String message) {
        messages.get(Status.unknown).add(ObjectUtil.defaultIfEmpty(message, "No message"));

        return this;
    }

    /**
     * Marks this status as unknown because of the given exception.
     */
    public ActionStatus unknown(Throwable exception) {
        this.exception = exception;
        LoggerFactory.getLogger(getClass()).error("ActionStatus: Unknown", exception);
        return unknown(exception.getMessage() + " (" + exception.getClass().getName() + ")");
    }

    /**
     * Marks this status as unable to verify because of the given message.
     */
    public ActionStatus cannotVerify(String message) {
        messages.get(Status.cannotVerify).add(ObjectUtil.defaultIfEmpty(message, "No message"));

        return this;
    }

    /**
     * Returns the {@link liquibase.action.ActionStatus.Status} enum value based on what has been set on this object.
     * The priority order for a response is:
     * <ol>
     * <li>Nothing previously set: return unknown</li>
     * <li>Cannot verify message(s)</li>
     * <li>Unknown message(s)</li>
     * <li>Not applied message(s)</li>
     * <li>Incorrect message(s)</li>
     * <li>Complete</li>
     * </ol>
     */
    public Status getStatus() {
        if (messages.get(Status.cannotVerify).size() > 0) {
            return Status.cannotVerify;
        } else if (messages.get(Status.unknown).size() > 0) {
            return Status.unknown;
        } else if (messages.get(Status.notApplied).size() > 0) {
            return Status.notApplied;
        } else if (messages.get(Status.incorrect).size() > 0) {
            return Status.incorrect;
        } else {
            if (atLeastOneAssertion) {
                return Status.applied;
            } else {
                return Status.unknown;
            }
        }
    }

    public String getMessage() {
        Status status = getStatus();
        if (status == Status.applied) {
            return null;
        } else {
            SortedSet<String> statusMessages = messages.get(status);
            if (statusMessages.size() == 0) {
                return null;
            } else {
                return StringUtils.join(statusMessages, ", ");
            }
        }
    }

    /**
     * Convenience method to check that the status is {@link Status#applied}
     */
    public boolean isApplied() {
        return getStatus() == Status.applied;
    }

    public Throwable getException() {
        return exception;
    }

    @Override
    public String toString() {
        String out = getStatus().name;

        String message = getMessage();
        if (message != null) {
            out += ": " + message;
        }

        return out;
    }

    public enum Status {
        applied("Applied"),
        incorrect("Incorrect"),
        notApplied("Not Applied"),
        unknown("Unknown"),
        cannotVerify("Cannot Verify");

        private String name;

        Status(String name) {
            this.name = name;
        }
    }
}
