package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectName;

import java.util.List;

public class AddPrimaryKeyAction extends AbstractAction {

    public ObjectName tableName;
    public String tablespace;
    public List<String> columnNames;
    public String constraintName;
    public Boolean clustered;
}
