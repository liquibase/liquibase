package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectName;

public class RenameSequenceAction extends AbstractAction {
        public ObjectName oldSequenceName;
        public ObjectName newSequenceName;
}
