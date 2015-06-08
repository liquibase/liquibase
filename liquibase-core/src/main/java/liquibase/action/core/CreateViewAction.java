package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectName;

public class CreateViewAction extends AbstractAction {

    public ObjectName viewName;
    public StringClauses selectQuery;
    public Boolean replaceIfExists;
    public Boolean fullDefinition;

}
