package liquibase.action.core;

import liquibase.action.AbstractAction;

import java.math.BigInteger;

public class AlterSequenceAction extends AbstractAction {
    
    public static enum Attr {
        catalogName,
        schemaName,
        sequenceName,
        incrementBy,
        maxValue,
        minValue,
        ordered,
    }
}
