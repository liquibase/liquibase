package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectName;

import java.math.BigInteger;

public class AlterSequenceAction extends AbstractAction {

    public ObjectName sequenceName;
    public BigInteger incrementBy;
    public BigInteger maxValue;
    public BigInteger minValue;
    public Boolean ordered;
}
