package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectName;

/**
 * Action to drop an existing sequence.
 */
public class DropSequenceAction extends AbstractAction {

    public ObjectName sequenceName;
}
