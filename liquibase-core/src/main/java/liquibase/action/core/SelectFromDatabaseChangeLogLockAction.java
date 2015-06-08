package liquibase.action.core;

import liquibase.action.AbstractAction;

import java.util.List;

public class SelectFromDatabaseChangeLogLockAction extends AbstractAction {

    public List<ColumnDefinition> selectColumnDefinitions;
}
