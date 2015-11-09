package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectReference;

import java.math.BigInteger;

public class AlterSequenceAction extends AbstractAction {

    public ObjectReference sequenceName;
    public BigInteger incrementBy;
    public BigInteger maxValue;
    public BigInteger minValue;
    public Boolean ordered;
}
