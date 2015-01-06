package liquibase.action.core;

import liquibase.action.AbstractAction;
import liquibase.action.QueryAction;

/**
 * Action to find all existing columns.
 * If "catalogName", "schemaName", "tableName" and/or "columnName" attributes are set, the returned columns should only include ones that match the set values.
 */
public class QueryColumnsMetaDataAction extends AbstractAction implements QueryAction {

    public static enum Attr {
        catalogName,
        schemaName,
        tableName,
        columnName,
    }

    public QueryColumnsMetaDataAction(String catalogName, String schemaName, String tableName, String columnName) {
        setAttribute(Attr.catalogName, catalogName);
        setAttribute(Attr.schemaName, schemaName);
        setAttribute(Attr.tableName, tableName);
        setAttribute(Attr.columnName, columnName);
    }
}
