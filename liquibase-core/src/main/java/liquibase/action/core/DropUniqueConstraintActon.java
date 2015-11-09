package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectReference;

import java.util.List;

public class DropUniqueConstraintActon extends AbstractAction {

    public ObjectReference tableName;
    public String constraintName;
    public List<String> uniqueColumnNames;
}
