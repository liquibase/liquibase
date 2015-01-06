package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.action.QueryAction;

/**
 * Action to find all existing tables.
 * If "catalogName", "schemaName", and/or "tableName" attributes are set, the returned columns should only include ones that match the set values.
 */
public class QueryTablesMetaDataAction extends AbstractAction implements QueryAction {

    public static enum Attr {
        catalogName,
        schemaName,
        tableName
    }

    public QueryTablesMetaDataAction(String catalogName, String schemaName, String tableName) {
        setAttribute(Attr.catalogName, catalogName);
        setAttribute(Attr.schemaName, schemaName);
        setAttribute(Attr.tableName, tableName);
    }
}
