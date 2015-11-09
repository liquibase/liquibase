package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectReference;

import java.util.List;

public class CopyRowsAction extends AbstractAction {

    public ObjectReference sourceTableName;

    public ObjectReference targetTableName;

    public List<String> sourceColumns;
    public List<String> targetColumns;
}
