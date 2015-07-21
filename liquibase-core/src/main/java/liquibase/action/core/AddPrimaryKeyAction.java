package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectName;
import liquibase.structure.core.PrimaryKey;

import java.util.List;

public class AddPrimaryKeyAction extends AbstractAction {

    public PrimaryKey primaryKey;

    public AddPrimaryKeyAction() {
    }

    public AddPrimaryKeyAction(PrimaryKey primaryKey) {
        this.primaryKey = primaryKey;
    }
}
