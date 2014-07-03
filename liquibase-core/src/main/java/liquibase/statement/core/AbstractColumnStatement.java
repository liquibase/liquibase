package liquibase.statement.core;

import liquibase.statement.AbstractStatement;
import liquibase.statement.Statement;

/**
 * Convenience base class for Statements that work on a column.
 */
abstract class AbstractColumnStatement extends AbstractTableStatement {

    public static final String COLUMN_NAME = "columnName";

    protected AbstractColumnStatement() {
    }

    public AbstractColumnStatement(String catalogName, String schemaName, String tableName, String columnName) {
        super(catalogName, schemaName, tableName);
        setColumnName(columnName);
    }

    public String getColumnName() {
        return getAttribute(COLUMN_NAME, String.class);
    }

    public Statement setColumnName(String columnName) {
        return (Statement) setAttribute(COLUMN_NAME, columnName);
    }



}
