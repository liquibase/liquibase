package liquibase.statement.core;

import liquibase.change.ColumnConfig;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Table;

import java.util.*;

/**
 * Inserts data into an existing table.
 */
public class InsertDataStatement extends AbstractTableStatement {
    public static final String COLUMN_VALUES = "columnValues";
    public static final String NEEDS_PREPARED_STATEMENT = "needsPreparedStatement";

    public InsertDataStatement() {
    }

    public InsertDataStatement(String catalogName, String schemaName, String tableName) {
        super(catalogName, schemaName, tableName);
    }

    protected void init() {
        setAttribute(COLUMN_VALUES, new HashMap());
    }

    public InsertDataStatement addColumnValue(String columnName, Object newValue) {
        getAttribute(COLUMN_VALUES, Map.class).put(columnName, newValue);

        return this;
    }

    /**
     * Returns the value of the given columnName. Returns null if not defined.
     */
    public Object getColumnValue(String columnName) {
        return getAttribute(COLUMN_VALUES, Map.class).get(columnName);
    }

    /**
     * Returns unmodifiable set of column names.
     */
    public Set<String> getColumnNames() {
        return Collections.unmodifiableSet(getAttribute(COLUMN_VALUES, Map.class).keySet());

    }

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return new DatabaseObject[] {
            new Table().setName(getTableName()).setSchema(getCatalogName(), getSchemaName())
        };
    }

    /**
     * Returns whether this insert statement requires use of a prepared statement.
     * Default value is false.
     */
    public boolean getNeedsPreparedStatement() {
        return getAttribute(NEEDS_PREPARED_STATEMENT, false);
    }

    public InsertDataStatement setNeedsPreparedStatement(boolean needsPreparedStatement) {
        return (InsertDataStatement) setAttribute(NEEDS_PREPARED_STATEMENT, needsPreparedStatement);
    }

}
