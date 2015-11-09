package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectReference;
import liquibase.util.StringClauses;

public class RedefineSequenceAction extends AbstractAction {

    public ObjectReference sequenceName;
    public StringClauses newDefinition;

    public RedefineSequenceAction() {
    }

    public RedefineSequenceAction(ObjectReference sequenceName, StringClauses newDefinition) {
        this.sequenceName = sequenceName;
        this.newDefinition = newDefinition;
    }

}
