package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectName;

public class AddUniqueConstraintAction extends AbstractAction {

    public static enum Attr {
        tableName,
        columnNames,
        constraintName,
        tablespace,

        deferrable,
        initiallyDeferred,
        disabled,

    }

    public AddUniqueConstraintAction() {

    }

    public AddUniqueConstraintAction(ObjectName tableName, String constraintName, String[] columnNames) {
        set(Attr.tableName, tableName);
        set(Attr.columnNames, columnNames);
        set(Attr.constraintName, constraintName);
    }

}
