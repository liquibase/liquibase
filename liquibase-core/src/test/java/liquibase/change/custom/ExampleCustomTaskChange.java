package liquibase.change.custom;

import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.RollbackImpossibleException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.logging.LogService;
import liquibase.logging.LogType;
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

    @Override
    public void execute(Database database) throws CustomChangeException {
        LogService.getLog(getClass()).info(LogType.LOG, "Hello "+getHelloTo());
    }

    @Override
    public void rollback(Database database) throws CustomChangeException, RollbackImpossibleException {
        LogService.getLog(getClass()).info(LogType.LOG, "Goodbye "+getHelloTo());
    }

    @Override
    public String getConfirmationMessage() {
        return "Said Hello";
    }

    @Override
    public void setUp() throws SetupException {
        ;
    }

    @Override
    public void setFileOpener(ResourceAccessor resourceAccessor) {
        this.resourceAccessor = resourceAccessor;
    }

    @Override
    public ValidationErrors validate(Database database) {
        return new ValidationErrors();
    }
}
