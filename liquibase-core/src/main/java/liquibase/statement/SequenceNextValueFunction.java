package liquibase.statement;

/**
 * Represents a function for getting the next value from a sequence
 */
public class SequenceNextValueFunction extends DatabaseFunction {

    public SequenceNextValueFunction() {
        this("UNSET");
    }

    public SequenceNextValueFunction(String sequenceName) {
        super(sequenceName);
    }
}