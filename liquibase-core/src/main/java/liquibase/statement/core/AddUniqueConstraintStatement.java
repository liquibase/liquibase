package liquibase.statement.core;

import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Table;
import liquibase.structure.core.UniqueConstraint;
import liquibase.util.StringUtils;

public class AddUniqueConstraintStatement extends AbstractUniqueConstraintStatement {

    public static final String TABLESPACE = "tablespace";
    public static final String COLUMN_NAMES = "columnNames";

    public static final String DEFERRABLE = "deferrable";
    public static final String INITIALLY_DEFERRED = "initiallyDeferred";
    public static final String DISABLED = "disabled";

    public AddUniqueConstraintStatement() {
    }

    public AddUniqueConstraintStatement(String constraintName, String catalogName, String schemaName, String tableName, String columnNames) {
        super(constraintName, catalogName, schemaName, tableName);
        setColumnNames(columnNames);
    }

    public String getTablespace() {
        return getAttribute(TABLESPACE, String.class);
    }

    public AddUniqueConstraintStatement setTablespace(String tablespace) {
        return (AddUniqueConstraintStatement) setAttribute(TABLESPACE, tablespace);
    }

    public String getColumnNames() {
        return getAttribute(COLUMN_NAMES, String.class);
    }

    public AddUniqueConstraintStatement setColumnNames(String columnNames) {
        return (AddUniqueConstraintStatement) setAttribute(COLUMN_NAMES, columnNames);
    }

    public boolean isDeferrable() {
        return getAttribute(DEFERRABLE, false);
    }

    public AddUniqueConstraintStatement setDeferrable(boolean deferrable) {
        return (AddUniqueConstraintStatement) setAttribute(DEFERRABLE, deferrable);
    }

    public boolean isInitiallyDeferred() {
        return getAttribute(INITIALLY_DEFERRED, false);
    }

    public AddUniqueConstraintStatement setInitiallyDeferred(boolean initiallyDeferred) {
        return (AddUniqueConstraintStatement) setAttribute(INITIALLY_DEFERRED, initiallyDeferred);
    }

    public boolean isDisabled() {
        return getAttribute(DISABLED, false);
    }

    public AddUniqueConstraintStatement setDisabled(boolean disabled) {
        return (AddUniqueConstraintStatement) setAttribute(DISABLED, disabled);
    }

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        UniqueConstraint uniqueConstraint = new UniqueConstraint()
                .setName(getConstraintName())
                .setTable((Table) new Table().setName(getTableName()).setSchema(getCatalogName(), getSchemaName()));
        int i = 0;
        for (String column : StringUtils.splitAndTrim(getColumnNames(), ",")) {
            uniqueConstraint.addColumn(i++, column);
        }

        return new DatabaseObject[]{
                uniqueConstraint
        };
    }
}
