package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectName;

import java.util.List;

public class AddColumnsAction extends AbstractAction {

    public ObjectName tableName;
    public List<ColumnDefinition> columnDefinitions;
    public List<UniqueConstraintDefinition> uniqueConstraintDefinitions;
    public List<ForeignKeyDefinition> foreignKeyDefinitions;

}
