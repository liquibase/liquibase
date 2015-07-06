package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectName;
import liquibase.structure.core.ForeignKey;

import java.util.List;

public class AddForeignKeyConstraintAction extends AbstractAction {

    public ForeignKey foreignKey;

    public AddForeignKeyConstraintAction() {
    }

    public AddForeignKeyConstraintAction(ForeignKey foreignKey) {
        this.foreignKey = foreignKey;
    }
}