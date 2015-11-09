package liquibase.structure.core;

import liquibase.structure.AbstractDatabaseObject;
import liquibase.structure.ObjectReference;

import java.math.BigInteger;

public class Sequence extends AbstractDatabaseObject {

    public BigInteger startValue;
    public BigInteger incrementBy;
    public BigInteger minValue;
    public BigInteger maxValue;
    public Boolean willCycle;
    public Boolean ordered;
    public BigInteger lastReturnedValue;
    public BigInteger cacheSize;

    public Sequence() {
    }

    public Sequence(String name) {
        super(name);
    }

    public Sequence(ObjectReference nameAndContainer) {
        super(nameAndContainer);
    }

    public Sequence(ObjectReference container, String name) {
        super(container, name);
    }
}
