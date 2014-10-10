package liquibase.statement;

public abstract class AbstractSqlStatement implements SqlStatement {

    @Override
    public boolean skipOnUnsupported() {
        return false;
    }
}
