package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.structure.ObjectName;

public class AddLookupTableAction extends AbstractAction {
    public ObjectName existingColumnName;

    public ObjectName newColumnName;
    public String newColumnDataType;
    public String constraintName;
}
