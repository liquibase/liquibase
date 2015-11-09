package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectReference;
import liquibase.structure.core.Column;
import liquibase.structure.core.DataType;

public class AddAutoIncrementAction extends AbstractAction {

    public ObjectReference columnName;
    public DataType columnDataType;
    public Column.AutoIncrementInformation autoIncrementInformation;

    public AddAutoIncrementAction() {
    }

    public AddAutoIncrementAction(ObjectReference columnName, DataType columnDataType, Column.AutoIncrementInformation autoIncrementInformation) {
        this.columnName = columnName;
        this.columnDataType = columnDataType;
        this.autoIncrementInformation = autoIncrementInformation;
    }
}
