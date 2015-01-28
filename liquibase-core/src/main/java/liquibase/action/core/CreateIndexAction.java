package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.change.AddColumnConfig;

public class CreateIndexAction extends AbstractAction {
    
    public static enum Attr {
        tableCatalogName,
        tableSchemaName,
        indexName,
        tableName,
        columnDefinitions,
        tablespace,
        unique,
        clustered,

    }
}
