package liquibase.action.core;

import liquibase.Scope;
import liquibase.action.AbstractAction;
import liquibase.action.ActionStatus;
import liquibase.snapshot.SnapshotFactory;
import liquibase.structure.ObjectName;
import liquibase.structure.core.Column;

import java.math.BigInteger;

public class AddAutoIncrementAction extends AbstractAction {

    public ObjectName columnName;
    public String columnDataType;
    public BigInteger startWith;
    public BigInteger incrementBy;

}
