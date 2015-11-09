package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectReference;

public class AddLookupTableAction extends AbstractAction {
    public ObjectReference existingColumnName;

    public ObjectReference newColumnName;
    public String newColumnDataType;
    public String constraintName;
}
