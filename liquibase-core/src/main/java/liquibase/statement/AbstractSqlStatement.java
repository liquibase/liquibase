package liquibase.statement;

public abstract class AbstractSqlStatement implements SqlStatement {

    private boolean continueOnError = false;


    @Override
    public boolean skipOnUnsupported() {
        return false;
    }

    @Override
    public boolean continueOnError() {
        return continueOnError;
    }

    public void setContinueOnError(boolean continueOnError) {
        this.continueOnError = continueOnError;
    }

}
