package liquibase.statement;

/**
 * Describes a function for getting the current value of a sequence, used in {@link liquibase.statement.Statement} objects.
 */
public class SequenceCurrentValueFunction extends DatabaseFunction {

    public SequenceCurrentValueFunction(String sequenceName) {
        super(sequenceName);
    }
}