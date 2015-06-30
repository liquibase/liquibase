package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectName;
import liquibase.util.StringClauses;

public class CreateViewAction extends AbstractAction {

    public ObjectName viewName;
    public StringClauses selectQuery;
    public Boolean replaceIfExists;
    public Boolean fullDefinition;

}
