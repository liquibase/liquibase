package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectName;

import java.util.List;

public class InsertOrUpdateDataAction extends AbstractAction {
    public List<String> primaryKeyColumnNames;
    public Boolean onlyUpdate;
    public ObjectName tableName;
    public List<String> columnNames;
    public List<Object> columnValues;
}
