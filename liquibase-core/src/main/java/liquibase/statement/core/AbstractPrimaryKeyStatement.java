package liquibase.statement.core;

public abstract class AbstractPrimaryKeyStatement extends AbstractTableStatement {

    public static final String CONSTRAINT_NAME = "constraintName";

    protected AbstractPrimaryKeyStatement() {
    }

    protected AbstractPrimaryKeyStatement(String constraintName, String catalogName, String schemaName, String tableName) {
        super(catalogName, schemaName, tableName);
        setConstraintName(constraintName);
    }

    public String getConstraintName() {
        return getAttribute(CONSTRAINT_NAME, String.class);
    }

    public AbstractPrimaryKeyStatement setConstraintName(String constraintName) {
        return (AbstractPrimaryKeyStatement) setAttribute(CONSTRAINT_NAME, constraintName);
    }


}
