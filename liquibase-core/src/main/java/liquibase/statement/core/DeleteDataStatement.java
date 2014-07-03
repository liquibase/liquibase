package liquibase.statement.core;

import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Deletes data from an existing table.
 */
public class DeleteDataStatement extends AbstractTableStatement {
    private static final String WHERE = "where";
    private static final String WHERE_PARAMETERS = "whereParameters";
    private static final String WHERE_COLUMN_NAMES = "whereColumnNames";

    public DeleteDataStatement() {
    }

    public DeleteDataStatement(String catalogName, String schemaName, String tableName) {
        super(catalogName, schemaName, tableName);
    }

    protected void init() {
        setAttribute(WHERE_PARAMETERS, new ArrayList());
        setAttribute(WHERE_COLUMN_NAMES, new ArrayList());
    }

    /**
     * The where clause to limit the data deleted.
     * Can include "?" or ":value" strings that are replaced in order by the values from {@link #getWhereParameters()}.
     * Can include ":name" strings that are replaced in order by the values from {@link #getWhereColumnNames()}.
     **/
    public String getWhere() {
        return getAttribute(WHERE, String.class);
    }

    public DeleteDataStatement setWhere(String where) {
        return (DeleteDataStatement) setAttribute(WHERE, where);
    }

    /**
     * Returns list of values to substitute into the value returned from {@link #getWhere()}.
     * This collection can be modified directly or with {@link #addWhereParameters(Object...)}.
     */
    public List<Object> getWhereParameters() {
        return getAttribute(WHERE_PARAMETERS, List.class);
    }

    public DeleteDataStatement addWhereParameters(Object... value) {
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


    public DeleteDataStatement addWhereColumnNames(String... name) {
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
