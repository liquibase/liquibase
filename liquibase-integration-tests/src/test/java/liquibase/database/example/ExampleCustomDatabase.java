package liquibase.database.example;

import liquibase.database.core.H2Database;
import liquibase.exception.DatabaseException;
import liquibase.logging.LogFactory;
import liquibase.logging.LogTarget;

public class ExampleCustomDatabase extends H2Database {

    @Override
    public int getPriority() {
        return super.getPriority()+5;
    }

    @Override
    public void tag(String tagString) throws DatabaseException {
        LogFactory.getLog(getClass()).info(LogTarget.LOG, "Custom tagging");
        super.tag(tagString);
    }
}
