package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectName;

public class CreateProcedureAction extends AbstractAction {
        public ObjectName procedureName;
        public StringClauses procedureText;
        public String endDelimiter;
        public Boolean replaceIfExists;
}
