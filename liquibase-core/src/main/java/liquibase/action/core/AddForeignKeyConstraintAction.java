package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectName;

import java.util.List;

public class AddForeignKeyConstraintAction extends AbstractAction {

    public ObjectName baseTableName;
    public String baseColumnNames;

    public ObjectName referencedTableName;
    public String referencedColumnNames;

    public String constraintName;

    public Boolean deferrable;
    public Boolean initiallyDeferred;

    public String onDelete;
    public Boolean onUpdate;

    public AddForeignKeyConstraintAction() {
    }

    public AddForeignKeyConstraintAction(String constraintName, ObjectName baseTableName, String baseColumnNames, ObjectName referencedTableName, String referencedColumnNames) {
        this.constraintName = constraintName;
        this.baseTableName = baseTableName;
        this.baseColumnNames = baseColumnNames;
        this.referencedTableName = referencedTableName;
        this.referencedColumnNames = referencedColumnNames;


    }
}