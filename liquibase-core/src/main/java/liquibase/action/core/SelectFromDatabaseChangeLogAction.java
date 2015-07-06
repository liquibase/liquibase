package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.core.Column;
import liquibase.util.CollectionUtil;
import liquibase.util.StringClauses;

import java.util.Arrays;
import java.util.List;

public class SelectFromDatabaseChangeLogAction extends AbstractAction {

    public List<Column> selectColumns;
    public StringClauses where;
    public List<String> orderByColumnNames;
    public Integer limit;

    public SelectFromDatabaseChangeLogAction() {
    }

    public SelectFromDatabaseChangeLogAction(Column... selectColumnNames) {
        this(Arrays.asList(CollectionUtil.createIfNull(selectColumnNames)));

    }

    public SelectFromDatabaseChangeLogAction(List<Column> selectColumnNames) {
        this.selectColumns = selectColumnNames;
    }
}
