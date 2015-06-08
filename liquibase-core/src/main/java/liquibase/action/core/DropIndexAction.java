package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectName;

public class DropIndexAction extends AbstractAction {

    public ObjectName indexName;
    public ObjectName tableName;
    public ObjectName associatedWith;

}
