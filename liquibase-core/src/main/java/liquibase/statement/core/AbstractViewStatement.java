package liquibase.statement.core;

import liquibase.statement.AbstractStatement;
import liquibase.statement.Statement;

/**
 * Convenience base class for Statements that work on a view.
 */
abstract class AbstractViewStatement extends AbstractStatement {

    public static final String CATALOG_NAME = "catalogName";
    public static final String SCHEMA_NAME = "schemaName";
    public static final String VIEW_NAME = "viewName";

    protected AbstractViewStatement() {
    }

    public AbstractViewStatement(String catalogName, String schemaName, String viewName) {
        setCatalogName(catalogName);
        setSchemaName(schemaName);
        setViewName(viewName);
    }

    public String getCatalogName() {
        return getAttribute(CATALOG_NAME, String.class);
    }

    public Statement setCatalogName(String catalogName) {
        return (Statement) setAttribute(CATALOG_NAME, catalogName);
    }

    public String getSchemaName() {
        return getAttribute(SCHEMA_NAME, String.class);
    }

    public Statement setSchemaName(String schemaName) {
        return (Statement) setAttribute(SCHEMA_NAME, schemaName);
    }

    public String getViewName() {
        return getAttribute(VIEW_NAME, String.class);
    }

    public Statement setViewName(String viewName) {
        return (Statement) setAttribute(VIEW_NAME, viewName);
    }
}
