package liquibase.preconditions;

import liquibase.database.Database;
import liquibase.exception.CustomPreconditionFailedException;

public class ExampleCustomPrecondition implements CustomPrecondition {
    public void check(Database database) throws CustomPreconditionFailedException {
        System.out.println("Custom precondition ran");

//        throw new CustomPreconditionFailedException("custom precondition failed", new RuntimeException());
    }
}
