package liquibase.statement;

/**
 * Represents a function for getting the current value from a sequence
 */
public class SequenceCurrentValueFunction extends DatabaseFunction {

    public SequenceCurrentValueFunction(String sequenceName) {
        super(sequenceName);
    }
}