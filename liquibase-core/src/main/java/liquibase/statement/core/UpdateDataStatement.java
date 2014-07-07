package liquibase.statement.core;

import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Table;

import java.util.*;

public class UpdateDataStatement extends AbstractTableStatement {

    public static final String NEW_COLUMN_VALUES = "newColumnValues";

    public static final String WHERE = "where";
    public static final String WHERE_PARAMETERS = "whereParameters";
    public static final String WHERE_COLUMN_NAMES = "whereColumnNames";

    public static final String NEEDS_PREPARED_STATEMENT = "needsPreparedStatement";

    public UpdateDataStatement() {
    }

    public UpdateDataStatement(String catalogName, String schemaName, String tableName) {
        super(catalogName, schemaName, tableName);
    }

    protected void init() {
        setAttribute(NEW_COLUMN_VALUES, new TreeMap());
        setAttribute(WHERE_PARAMETERS, new ArrayList());
        setAttribute(WHERE_COLUMN_NAMES, new ArrayList());
    }

    public SortedSet<String> getColumnNames() {
        return (SortedSet) getAttribute(NEW_COLUMN_VALUES, Map.class).keySet();
    }

    public Object getNewColumnValue(String columnName) {
        return getAttribute(NEW_COLUMN_VALUES, Map.class).get(columnName);
    }

    public UpdateDataStatement addNewColumnValue(String columnName, Object newValue) {
        getAttribute(NEW_COLUMN_VALUES, Map.class).put(columnName, newValue);

        return this;
    }

    public UpdateDataStatement removeNewColumnValue(String columnName) {
        getAttribute(NEW_COLUMN_VALUES, Map.class).remove(columnName);

        return this;
    }


    /**
     * Returns whether this update statement requires use of a prepared statement.
     * Default value is false.
     */
    public boolean getNeedsPreparedStatement() {
        return getAttribute(NEEDS_PREPARED_STATEMENT, false);
    }

    public UpdateDataStatement setNeedsPreparedStatement(boolean needsPreparedStatement) {
        return (UpdateDataStatement) setAttribute(NEEDS_PREPARED_STATEMENT, needsPreparedStatement);
    }

    /**
     * The where clause to limit the data deleted.
     * Can include "?" or ":value" strings that are replaced in order by the values from {@link #getWhereParameters()}.
     * Can include ":name" strings that are replaced in order by the values from {@link #getWhereColumnNames()}.
     **/
    public String getWhere() {
        return getAttribute(WHERE, String.class);
    }

    public UpdateDataStatement setWhere(String whereClause) {
        return (UpdateDataStatement) setAttribute(WHERE, whereClause);
    }

    /**
     * Returns list of values to substitute into the value returned from {@link #getWhere()}.
     * This collection can be modified directly or with {@link #addWhereParameters(Object...)}.
     */
    public List<Object> getWhereParameters() {
        return getAttribute(WHERE_PARAMETERS, List.class);
    }

    public UpdateDataStatement addWhereParameters(Object... value) {
        if (value != null) {
            getWhereParameters().addAll(Arrays.asList(value));
        }
        return this;
    }

    /**
     * Returns list of column names to substitute into the value returned from {@link #getWhere()}.
     * This collection can be modified directly or with {@link #addWhereColumnNames(String...)}.
     */
    public List<String> getWhereColumnNames() {
        return getAttribute(WHERE_COLUMN_NAMES, List.class);
    }


    public UpdateDataStatement addWhereColumnNames(String... name) {
        if (name != null) {
            getWhereColumnNames().addAll(Arrays.asList(name));
        }
        return this;
    }

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return new DatabaseObject[] {
            new Table().setName(getTableName()).setSchema(getCatalogName(), getSchemaName())
        };
    }
}
