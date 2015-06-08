package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectName;

import java.util.List;

public class DropUniqueConstraintActon extends AbstractAction {

    public ObjectName tableName;
    public String constraintName;
    public List<String> uniqueColumnNames;
}
