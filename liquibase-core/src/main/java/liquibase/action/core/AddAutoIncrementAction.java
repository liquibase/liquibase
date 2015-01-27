package liquibase.action.core;

import liquibase.action.AbstractAction;

import java.math.BigInteger;

public class AddAutoIncrementAction extends AbstractAction {

    public static enum Attr {
        catalogName,
        schemaName,
        tableName,
        columnName,
        columnDataType,
        startWith,
        incrementBy,
    }
}
