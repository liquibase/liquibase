package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectReference;
import liquibase.structure.core.UniqueConstraint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AddUniqueConstraintsAction extends AbstractAction {

    public List<UniqueConstraint> uniqueConstraints = new ArrayList<>();

    public AddUniqueConstraintsAction() {

    }

    public AddUniqueConstraintsAction(UniqueConstraint... uniqueConstraints) {
        if (uniqueConstraints != null) {
            this.uniqueConstraints.addAll(Arrays.asList(uniqueConstraints));
        }
    }
}
