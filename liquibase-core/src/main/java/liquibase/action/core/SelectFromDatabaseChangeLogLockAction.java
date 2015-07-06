package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.core.Column;

import java.util.List;

public class SelectFromDatabaseChangeLogLockAction extends AbstractAction {

    public List<Column> selectColumns;
}
