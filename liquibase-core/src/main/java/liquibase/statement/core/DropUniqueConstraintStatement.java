package liquibase.statement.core;

import liquibase.statement.AbstractStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Table;
import liquibase.structure.core.UniqueConstraint;
import liquibase.util.StringUtils;

public class DropUniqueConstraintStatement extends AbstractUniqueConstraintStatement {

    public static final String COLUMN_NAMES = "columnNames";

    public DropUniqueConstraintStatement() {
    }

    public DropUniqueConstraintStatement(String constraintName, String catalogName, String schemaName, String tableName) {
        super(constraintName, catalogName, schemaName, tableName);
    }

    public String getColumnNames() {
        return getAttribute(COLUMN_NAMES, String.class);
    }

    public DropUniqueConstraintStatement setColumnNames(String uniqueColumns) {
        return (DropUniqueConstraintStatement) setAttribute(COLUMN_NAMES, uniqueColumns);
    }

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        UniqueConstraint constraint = new UniqueConstraint().setName(getConstraintName()).setTable((Table) new Table().setName(getTableName()).setSchema(getCatalogName(), getSchemaName()));
        if (getColumnNames() != null) {
            int i = 0;
            for (String column : StringUtils.splitAndTrim(getColumnNames(), ",")) {
                constraint.addColumn(i++, column);
            }
        }
        return new DatabaseObject[]{
                constraint
        };
    }
}
