package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectName;
import liquibase.structure.core.UniqueConstraint;

import java.util.List;

public class AddUniqueConstraintAction extends AbstractAction {

    public UniqueConstraint uniqueConstraint;

    public AddUniqueConstraintAction() {

    }

    public AddUniqueConstraintAction(ObjectName name, String... columnNames) {
        this(new UniqueConstraint(name, columnNames));
    }

    public AddUniqueConstraintAction(UniqueConstraint uniqueConstraint) {
        this.uniqueConstraint = uniqueConstraint;
    }
}
