package liquibase.action.core;

import liquibase.action.AbstractAction;

/**
 * Action to create a new sequence.
 */
public class CreateSequenceAction extends AbstractAction {

    public static enum Attr {
        catalogName,
        schemaName,
        sequenceName,
        startValue,
        incrementBy,
        maxValue,
        minValue,
        ordered,
        cycle,
        cacheSize
    }
}
