package liquibase.change.custom;

import liquibase.database.Database;
import liquibase.exception.*;
import liquibase.resource.ResourceAccessor;

public class ExampleCustomTaskChange implements CustomTaskChange, CustomTaskRollback {

    private String helloTo;

    @SuppressWarnings({"UnusedDeclaration", "FieldCanBeLocal"})
    private ResourceAccessor resourceAccessor;


    public String getHelloTo() {
        return helloTo;
    }

    public void setHelloTo(String helloTo) {
        this.helloTo = helloTo;
    }

    public void execute(Database database) throws CustomChangeException {
        System.out.println("Hello "+getHelloTo());
    }

    public void rollback(Database database) throws CustomChangeException, RollbackImpossibleException {
        System.out.println("Goodbye "+getHelloTo());
    }

    public String getConfirmationMessage() {
        return "Said Hello";
    }

    public void setUp() throws SetupException {
        ;
    }

    public void setFileOpener(ResourceAccessor resourceAccessor) {
        this.resourceAccessor = resourceAccessor;
    }

    public ValidationErrors validate(Database database) {
        return new ValidationErrors();
    }
}
