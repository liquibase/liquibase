package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectReference;
import liquibase.util.StringClauses;

public class CreateViewAction extends AbstractAction {

    public ObjectReference viewName;
    public StringClauses selectQuery;
    public Boolean replaceIfExists;
    public Boolean fullDefinition;

}
