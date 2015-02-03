package liquibase.action.core;

import liquibase.action.AbstractAction;

public class AddLookupTableAction extends AbstractAction {
    public static enum Attr {
        existingTableCatalogName,
        existingTableSchemaName,
        existingTableName,
        existingColumnName,

        newTableCatalogName,
        newTableSchemaName,
        newTableName,
        newColumnName,
        newColumnDataType,
        constraintName,

    }
}
