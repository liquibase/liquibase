package liquibase.action.core;

import liquibase.action.AbstractAction;

public class RedefineSequenceAction extends AbstractAction {

    public static enum Attr {
        catalogName,
        schemaName,
        sequenceName,
        newDefinition
    }

    public RedefineSequenceAction() {
    }

    public RedefineSequenceAction(String catalogName, String schemaName, String sequenceName, StringClauses newDefinition) {
        set(Attr.catalogName, catalogName);
        set(Attr.schemaName, schemaName);
        set(Attr.sequenceName, sequenceName);
        set(Attr.newDefinition, newDefinition);
    }

}
