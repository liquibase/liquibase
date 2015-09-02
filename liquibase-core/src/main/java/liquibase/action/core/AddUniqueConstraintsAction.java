package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectName;
import liquibase.structure.core.UniqueConstraint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AddUniqueConstraintsAction extends AbstractAction {

    public List<UniqueConstraint> uniqueConstraints = new ArrayList<>();

    public AddUniqueConstraintsAction() {

    }

    public AddUniqueConstraintsAction(ObjectName name, String... columnNames) {
        this(new UniqueConstraint(name, columnNames));
    }

    public AddUniqueConstraintsAction(UniqueConstraint... uniqueConstraints) {
        if (uniqueConstraints != null) {
            this.uniqueConstraints.addAll(Arrays.asList(uniqueConstraints));
        }
    }
}
