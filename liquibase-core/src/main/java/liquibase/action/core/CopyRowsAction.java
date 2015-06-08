package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectName;

import java.util.List;

public class CopyRowsAction extends AbstractAction {

    public ObjectName sourceTableName;

    public ObjectName targetTableName;

    public List<String> sourceColumns;
    public List<String> targetColumns;
}
