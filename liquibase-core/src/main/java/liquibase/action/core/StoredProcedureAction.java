package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectName;

import java.util.List;

public class StoredProcedureAction extends AbstractAction {

    public ObjectName procedureName;
    public List<String> parameterNames;
}
