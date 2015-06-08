package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectName;

import java.math.BigInteger;

/**
 * Action to create a new sequence.
 */
public class CreateSequenceAction extends AbstractAction {

        public ObjectName sequenceName;
        public BigInteger startValue;
        public BigInteger incrementBy;
        public BigInteger maxValue;
        public BigInteger minValue;
        public Boolean ordered;
        public Boolean cycle;
        public BigInteger cacheSize;

    public CreateSequenceAction() {
    }

    public CreateSequenceAction(ObjectName sequenceName) {
        this.sequenceName = sequenceName;
    }

}
