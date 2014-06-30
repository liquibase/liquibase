package liquibase.statement;

/**
 * Describes a function for getting the current date and time, used in {@link liquibase.statement.Statement} objects.
 */
public class CurrentDateTimeFunction extends DatabaseFunction {

    public CurrentDateTimeFunction() {
        super("CURRENT_TIME");
    }
}
