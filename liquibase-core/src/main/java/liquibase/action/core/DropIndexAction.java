package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectReference;

public class DropIndexAction extends AbstractAction {

    public ObjectReference indexName;
    public ObjectReference tableName;
    public ObjectReference associatedWith;

}
