package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectName;

/**
 * Action to create a new sequence.
 */
public class CreateSequenceAction extends AbstractAction {

    public static enum Attr {
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

    public CreateSequenceAction(ObjectName sequenceName) {
        set(Attr.sequenceName, sequenceName);
    }

}
