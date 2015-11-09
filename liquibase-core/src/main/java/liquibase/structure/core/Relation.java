package liquibase.structure.core;

import liquibase.structure.AbstractDatabaseObject;
import liquibase.structure.ObjectReference;

/**
 * A container of columns. Usually a table or view.
 */
public abstract class Relation extends AbstractDatabaseObject {

    public String remarks;

    public Relation() {
    }

    public Relation(String name) {
        super(name);
    }

    public Relation(ObjectReference nameAndContainer) {
        super(nameAndContainer);
    }

    public Relation(ObjectReference container, String name) {
        super(container, name);
    }
}
