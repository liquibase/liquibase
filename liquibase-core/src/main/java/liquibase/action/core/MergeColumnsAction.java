package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectReference;

public class MergeColumnsAction extends AbstractAction {
    public ObjectReference tableName;
    public String column1Name;
    public String joinString;
    public String column2Name;
    public String finalColumnName;
    public String finalColumnType;
}
