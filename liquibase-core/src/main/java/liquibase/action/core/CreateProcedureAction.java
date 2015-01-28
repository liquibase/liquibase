package liquibase.action.core;

import liquibase.action.AbstractAction;

public class CreateProcedureAction extends AbstractAction {
    public static enum Attr {
        catalogName,
        schemaName,
        procedureName,
        procedureText,
        endDelimiter,
        replaceIfExists,

    }
}
