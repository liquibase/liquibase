package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectName;

public class RedefineSequenceAction extends AbstractAction {

    public ObjectName sequenceName;
    public StringClauses newDefinition;

    public RedefineSequenceAction() {
    }

    public RedefineSequenceAction(ObjectName sequenceName, StringClauses newDefinition) {
        this.sequenceName = sequenceName;
        this.newDefinition = newDefinition;
    }

}
