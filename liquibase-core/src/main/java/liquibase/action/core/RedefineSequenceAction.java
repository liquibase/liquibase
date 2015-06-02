package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectName;

public class RedefineSequenceAction extends AbstractAction {

    public static enum Attr {
        sequenceName,
        newDefinition
    }

    public RedefineSequenceAction() {
    }

    public RedefineSequenceAction(ObjectName sequenceName, StringClauses newDefinition) {
        set(Attr.sequenceName, sequenceName);
        set(Attr.newDefinition, newDefinition);
    }

}
