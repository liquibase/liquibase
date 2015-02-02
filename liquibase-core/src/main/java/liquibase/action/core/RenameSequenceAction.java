package liquibase.action.core;

import liquibase.action.AbstractAction;

public class RenameSequenceAction extends AbstractAction {
    public static enum Attr {
        catalogName,
        schemaName,
        oldSequenceName,
        newSequenceName,
    }
}
