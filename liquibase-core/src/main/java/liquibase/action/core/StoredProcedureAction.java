package liquibase.action.core;

import liquibase.action.AbstractAction;

public class StoredProcedureAction extends AbstractAction {

    public static enum Attr {
        procedureName,
        parameterNames,
//        private List<Integer> types = new ArrayList<Integer>();

        }
}
