package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectName;
import liquibase.util.CollectionUtil;
import liquibase.util.StringClauses;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

public class SelectDataAction extends AbstractAction {

    public ObjectName tableName;
    public List<ColumnDefinition> selectColumnDefinitions;
    public StringClauses where;
    public List<String> orderByColumnNames;
    public Integer limit;

    public SelectDataAction() {
    }

    public SelectDataAction(ObjectName tableName, List<ColumnDefinition> selectColumnNames) {
        this.tableName = tableName;
        this.selectColumnDefinitions = selectColumnNames;
    }

    public SelectDataAction(ObjectName tableName, ColumnDefinition... selectColumnNames) {
        this.tableName = tableName;
        this.selectColumnDefinitions = Arrays.asList(CollectionUtil.createIfNull(selectColumnNames));
    }
}
