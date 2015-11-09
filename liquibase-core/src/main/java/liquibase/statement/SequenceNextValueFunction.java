package liquibase.statement;

import liquibase.structure.ObjectReference;

/**
 * Represents a function for getting the next value from a sequence
 */
public class SequenceNextValueFunction extends DatabaseFunction {

    public SequenceNextValueFunction() {
        super("UNSET");
    }

    public SequenceNextValueFunction(ObjectReference sequenceName) {
        super(sequenceName.toString());
    }
}