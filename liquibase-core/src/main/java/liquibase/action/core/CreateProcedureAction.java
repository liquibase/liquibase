package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectReference;
import liquibase.util.StringClauses;

public class CreateProcedureAction extends AbstractAction {
        public ObjectReference procedureName;
        public StringClauses procedureText;
        public String endDelimiter;
        public Boolean replaceIfExists;
}
