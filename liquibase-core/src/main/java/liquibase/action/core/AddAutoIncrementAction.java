package liquibase.action.core;

import liquibase.Scope;
import liquibase.action.AbstractAction;
import liquibase.action.ActionStatus;
import liquibase.snapshot.SnapshotFactory;
import liquibase.structure.ObjectName;
import liquibase.structure.core.Column;
import liquibase.structure.core.DataType;

import java.math.BigInteger;

public class AddAutoIncrementAction extends AbstractAction {

    public ObjectName columnName;
    public DataType columnDataType;
    public Column.AutoIncrementInformation autoIncrementInformation;

    public AddAutoIncrementAction() {
    }

    public AddAutoIncrementAction(ObjectName columnName, DataType columnDataType, Column.AutoIncrementInformation autoIncrementInformation) {
        this.columnName = columnName;
        this.columnDataType = columnDataType;
        this.autoIncrementInformation = autoIncrementInformation;
    }
}
