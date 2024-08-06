package liquibase.statement;

import lombok.Setter;

@Setter
public abstract class AbstractSqlStatement implements SqlStatement {

    private boolean continueOnError;


    @Override
    public boolean skipOnUnsupported() {
        return false;
    }

    @Override
    public boolean continueOnError() {
        return continueOnError;
    }

}
