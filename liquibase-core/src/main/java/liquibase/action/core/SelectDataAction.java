package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectName;
import liquibase.structure.core.Column;
import liquibase.util.CollectionUtil;
import liquibase.util.StringClauses;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

public class SelectDataAction extends AbstractAction {

    public ObjectName tableName;
    public List<Column> selectColumns;
    public StringClauses where;
    public List<String> orderByColumnNames;
    public Integer limit;

    public SelectDataAction() {
    }

    public SelectDataAction(ObjectName tableName, List<Column> selectColumnNames) {
        this.tableName = tableName;
        this.selectColumns = selectColumnNames;
    }

    public SelectDataAction(ObjectName tableName, Column... selectColumnNames) {
        this.tableName = tableName;
        this.selectColumns = Arrays.asList(CollectionUtil.createIfNull(selectColumnNames));
    }
}
