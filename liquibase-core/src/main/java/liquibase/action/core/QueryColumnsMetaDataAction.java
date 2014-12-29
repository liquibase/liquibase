package liquibase.action.core;

import liquibase.AbstractExtensibleObject;

public class QueryColumnsMetaDataAction extends AbstractExtensibleObject {

    public static enum Attributes {
        catalogName,
        schemaName,
        tableName,
        columnName,
    }

    public QueryColumnsMetaDataAction(String catalogName, String schemaName, String tableName, String columnName) {
        setAttribute(Attributes.catalogName, catalogName);
        setAttribute(Attributes.schemaName, schemaName);
        setAttribute(Attributes.tableName, tableName);
        setAttribute(Attributes.columnName, tableName);
    }

    public String getCatalogName() {
        return getAttribute(Attributes.catalogName, String.class);
    }

    public String getSchemaName() {
        return getAttribute(Attributes.schemaName, String.class);
    }

    public String getTableName() {
        return getAttribute(Attributes.tableName, String.class);
    }

    public String getColumnName() {
        return getAttribute(Attributes.columnName, String.class);
    }
}
