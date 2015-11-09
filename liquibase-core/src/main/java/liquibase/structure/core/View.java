package liquibase.structure.core;

import liquibase.structure.ObjectReference;

public class View extends Relation {

    public Boolean containsFullDefinition;
    public String definition;

    public View() {
    }

    public View(String name) {
        super(name);
    }

    public View(ObjectReference nameAndContainer) {
        super(nameAndContainer);
    }

    public View(ObjectReference container, String name) {
        super(container, name);
    }
}
