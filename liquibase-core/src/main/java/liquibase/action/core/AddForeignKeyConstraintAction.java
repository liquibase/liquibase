package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectName;

public class AddForeignKeyConstraintAction extends AbstractAction {

    public static enum Attr {
        baseTableName,
        baseColumnNames,

        referencedTableName,
        referencedColumnNames,

        constraintName,

        deferrable,
        initiallyDeferred,

        onDelete,
        onUpdate,
    }

    public AddForeignKeyConstraintAction() {
    }

    public AddForeignKeyConstraintAction(String constraintName, ObjectName baseTableName, String[] baseColumnNames, ObjectName referencedTableName, String[] referencedColumnNames) {
        set(Attr.constraintName, constraintName);
        set(Attr.baseTableName, baseTableName);
        set(Attr.baseColumnNames, baseColumnNames);
        set(Attr.referencedTableName, referencedTableName);
        set(Attr.referencedColumnNames, referencedColumnNames);


    }
}