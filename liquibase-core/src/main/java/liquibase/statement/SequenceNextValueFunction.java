package liquibase.statement;

/**
 * Describes a function for getting the current value of a sequence, used in {@link liquibase.statement.Statement} objects.
 */
public class SequenceNextValueFunction extends DatabaseFunction {

    public SequenceNextValueFunction() {
        super();
    }

    public SequenceNextValueFunction(String sequenceName) {
        super(sequenceName);
    }
}