package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectReference;

import java.math.BigInteger;

/**
 * Action to create a new sequence.
 */
public class CreateSequenceAction extends AbstractAction {

        public ObjectReference sequenceName;
        public BigInteger startValue;
        public BigInteger incrementBy;
        public BigInteger maxValue;
        public BigInteger minValue;
        public Boolean ordered;
        public Boolean cycle;
        public BigInteger cacheSize;

    public CreateSequenceAction() {
    }

    public CreateSequenceAction(ObjectReference sequenceName) {
        this.sequenceName = sequenceName;
    }

}
