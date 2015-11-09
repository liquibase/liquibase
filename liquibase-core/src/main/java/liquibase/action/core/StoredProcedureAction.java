package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectReference;

import java.util.List;

public class StoredProcedureAction extends AbstractAction {

    public ObjectReference procedureName;
    public List<String> parameterNames;
}
