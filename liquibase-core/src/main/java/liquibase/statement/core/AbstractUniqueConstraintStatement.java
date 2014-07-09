package liquibase.statement.core;

public abstract class AbstractUniqueConstraintStatement extends AbstractTableStatement {
    public static final String CONSTRAINT_NAME = "constraintName";

    protected AbstractUniqueConstraintStatement() {
    }

    public AbstractUniqueConstraintStatement(String constraintName, String catalogName, String schemaName, String tableName) {
        super(catalogName, schemaName, tableName);
        setConstraintName(constraintName);

    }

    public String getConstraintName() {
        return getAttribute(CONSTRAINT_NAME, String.class);
    }

    public AbstractUniqueConstraintStatement setConstraintName(String constraintName) {
        return (AbstractUniqueConstraintStatement) setAttribute(CONSTRAINT_NAME, constraintName);
    }
}
