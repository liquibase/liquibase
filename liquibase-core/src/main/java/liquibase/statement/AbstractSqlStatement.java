package liquibase.statement;

public abstract class AbstractSqlStatement implements SqlStatement {

    public boolean skipOnUnsupported() {
        return false;
    }
}
