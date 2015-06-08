package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.actionlogic.UpdateResult;
import liquibase.structure.ObjectName;
import liquibase.util.CollectionUtil;

import java.util.Arrays;
import java.util.List;

public class UpdateDataAction extends AbstractAction {

    public ObjectName tableName;
    public List<String> columnNames;
    public List<Object> newColumnValues;
    public StringClauses whereClause;
    public List<String> whereColumnNames;
    public List<Object> whereParameters;

    public UpdateDataAction() {
    }

    public UpdateDataAction(ObjectName tableName) {
        this.tableName = tableName;
    }

    public UpdateDataAction addNewColumnValue(String columnName, Object columnValue) {
        this.columnNames = CollectionUtil.createIfNull(this.columnNames, columnName);
        this.newColumnValues = CollectionUtil.createIfNull(this.newColumnValues, columnValue);

        return this;
    }

    public UpdateDataAction addWhereParameters(Object... parameters) {
        if (parameters != null) {
            whereParameters = CollectionUtil.createIfNull(this.whereParameters);
            whereParameters.addAll(Arrays.asList(parameters));
        }

        return this;
    }

}
