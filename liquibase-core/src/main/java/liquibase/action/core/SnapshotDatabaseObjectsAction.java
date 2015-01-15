package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.action.QueryAction;
import liquibase.structure.DatabaseObject;

public class SnapshotDatabaseObjectsAction extends AbstractAction implements QueryAction{

    public static enum Attr {
        typeToSnapshot,
        relatedTo
    }

    public SnapshotDatabaseObjectsAction(Class<? extends DatabaseObject> typeToLookup, DatabaseObject relatedTo) {
        this.setAttribute(Attr.typeToSnapshot, typeToLookup);
        this.setAttribute(Attr.relatedTo, relatedTo);
    }
}
