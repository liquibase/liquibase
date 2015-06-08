package liquibase.action.core;

import liquibase.AbstractExtensibleObject;

import java.math.BigInteger;

public class AutoIncrementDefinition extends AbstractExtensibleObject {

    public BigInteger startWith;
    public BigInteger incrementBy;

}
