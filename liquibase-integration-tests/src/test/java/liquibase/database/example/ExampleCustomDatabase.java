package liquibase.database.example;

import liquibase.Scope;
import liquibase.database.core.H2Database;
import liquibase.exception.DatabaseException;

public class ExampleCustomDatabase extends H2Database {

    @Override
    public int getPriority() {
        return super.getPriority()+5;
    }

    @Override
    public void tag(String tagString) throws DatabaseException {
        Scope.getCurrentScope().getLog(getClass()).info("Custom tagging");
        super.tag(tagString);
    }
}
