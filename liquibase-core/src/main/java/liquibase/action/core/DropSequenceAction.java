package liquibase.action.core;

import liquibase.action.AbstractAction;

/**
 * Action to drop an existing sequence.
 */
public class DropSequenceAction extends AbstractAction {

    public static enum Attr {
        catalogName,
        schemaName,
        sequenceName,
    }
}
