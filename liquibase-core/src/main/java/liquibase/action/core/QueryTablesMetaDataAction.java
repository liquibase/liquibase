package liquibase.action.core;

import liquibase.AbstractExtensibleObject;

public class QueryTablesMetaDataAction extends AbstractExtensibleObject {

    public static enum Attributes {
        catalogName,
        schemaName,
        tableName
    }

    public QueryTablesMetaDataAction(String catalogName, String schemaName, String tableName) {
        setAttribute(Attributes.catalogName, catalogName);
        setAttribute(Attributes.schemaName, schemaName);
        setAttribute(Attributes.tableName, tableName);
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
}
