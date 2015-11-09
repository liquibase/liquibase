package liquibase.structure.core;

import liquibase.structure.ObjectReference;

public class Table extends Relation {

    public String tablespace;

    public Table() {
    }

    public Table(String name) {
        super(name);
    }

    public Table(ObjectReference nameAndContainer) {
        super(nameAndContainer);
    }

    public Table(ObjectReference container, String name) {
        super(container, name);
    }
}
