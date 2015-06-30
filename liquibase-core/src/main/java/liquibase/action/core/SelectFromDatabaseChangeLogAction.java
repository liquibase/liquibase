package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.util.CollectionUtil;
import liquibase.util.StringClauses;

import java.util.Arrays;
import java.util.List;

public class SelectFromDatabaseChangeLogAction extends AbstractAction {

    public List<ColumnDefinition> selectColumnDefinitions;
    public StringClauses where;
    public List<String> orderByColumnNames;
    public Integer limit;

    public SelectFromDatabaseChangeLogAction() {
    }

    public SelectFromDatabaseChangeLogAction(ColumnDefinition... selectColumnNames) {
        this(Arrays.asList(CollectionUtil.createIfNull(selectColumnNames)));

    }

    public SelectFromDatabaseChangeLogAction(List<ColumnDefinition> selectColumnNames) {
        this.selectColumnDefinitions = selectColumnNames;
    }
}
