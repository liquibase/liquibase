package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectName;

public class MergeColumnsAction extends AbstractAction {
    public ObjectName tableName;
    public String column1Name;
    public String joinString;
    public String column2Name;
    public String finalColumnName;
    public String finalColumnType;
}
