package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectReference;

public class RenameSequenceAction extends AbstractAction {
        public ObjectReference oldSequenceName;
        public ObjectReference newSequenceName;
}
