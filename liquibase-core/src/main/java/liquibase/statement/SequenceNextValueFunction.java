package liquibase.statement;

import liquibase.structure.ObjectName;

/**
 * Represents a function for getting the next value from a sequence
 */
public class SequenceNextValueFunction extends DatabaseFunction {

    public SequenceNextValueFunction() {
        super("UNSET");
    }

    public SequenceNextValueFunction(ObjectName sequenceName) {
        super(sequenceName.toString());
    }
}