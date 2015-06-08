package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectName;

import java.util.List;

public class AddUniqueConstraintAction extends AbstractAction {

    public ObjectName tableName;
    public List<String> columnNames;
    public String constraintName;
    public String tablespace;

    public Boolean deferrable;
    public Boolean initiallyDeferred;
    public Boolean disabled;

    public AddUniqueConstraintAction() {

    }

    public AddUniqueConstraintAction(ObjectName tableName, String constraintName, List<String> columnNames) {
        this.tableName = tableName;
        this.columnNames = columnNames;
        this.constraintName = constraintName;
    }

}
