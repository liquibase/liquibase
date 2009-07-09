package liquibase.database.example;

import liquibase.database.core.H2Database;
import liquibase.exception.DatabaseException;

public class ExampleCustomDatabase extends H2Database {

    @Override
    public void tag(String tagString) throws DatabaseException {
        System.out.println("Custom tagging");
        super.tag(tagString);
    }
}
