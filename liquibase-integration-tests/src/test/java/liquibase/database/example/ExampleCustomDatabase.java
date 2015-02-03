package liquibase.database.example;

import liquibase.exception.DatabaseException;
import liquibase.sdk.database.MockDatabase;

public class ExampleCustomDatabase extends MockDatabase {

    @Override
    public int getPriority() {
        return super.getPriority()+5;
    }

    @Override
    public void tag(String tagString) throws DatabaseException {
        System.out.println("Custom tagging");
        super.tag(tagString);
    }
}
