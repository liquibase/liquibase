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

    public CreateSequenceAction() {
    }

    public CreateSequenceAction(String catalogName, String schemaName, String sequenceName) {
        set(Attr.catalogName, catalogName);
        set(Attr.schemaName, schemaName);
        set(Attr.sequenceName, sequenceName);
    }

}
