package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectName;
import liquibase.util.CollectionUtil;

import java.util.List;

public class InsertDataAction extends AbstractAction {

    public ObjectName tableName;
    public List<String> columnNames;
    public List<Object> columnValues;

    public InsertDataAction() {
    }

    public InsertDataAction(ObjectName tableName) {
        this.tableName = tableName;
    }

    public InsertDataAction addColumnValue(String columnName, Object columnValue) {
        columnNames = CollectionUtil.createIfNull(columnNames, columnName);
        columnValues = CollectionUtil.createIfNull(columnValues, columnValue);

        return this;
    }


}
