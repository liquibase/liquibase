package liquibase.statement;

public abstract class AbstractSqlStatement implements SqlStatement {

    private boolean isForLiquibaseObject = false;

    public boolean skipOnUnsupported() {
        return false;
    }

    /**
     * Whether this statement is for liquibase objects like the database change log table or lock table.
     * Used to determine whether object names should be quoted.
     * Liquibase object names should not be quoted to maintain backwards compatibility.
     */
    public boolean isForLiquibaseObject() {
        return isForLiquibaseObject;
    }

    public void setForLiquibaseObject(final boolean forLiquibaseObject) {
        this.isForLiquibaseObject = forLiquibaseObject;
    }
}
