package liquibase.change.custom;

import liquibase.Scope;
import liquibase.database.Database;
import liquibase.exception.CustomChangeException;
import liquibase.exception.RollbackImpossibleException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

import java.util.Map;

public class ExampleCustomTaskChange implements CustomTaskChange, CustomTaskRollback {
    public static String SCOPE_ATTR_CALL_COUNT_MAP  = "callCountMap";
    public static String COUNT_MAP_EXECUTE_CALL_COUNT  = "executeCallCount";
    public static String COUNT_MAP_ROLLBACK_CALL_COUNT  = "rollbackCallCount";

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
        Scope.getCurrentScope().getLog(getClass()).info("Hello " + getHelloTo());
        Map<String, Object> callCountMap = Scope.getCurrentScope().get(SCOPE_ATTR_CALL_COUNT_MAP, Map.class);
        if (callCountMap == null) {
            return;
        }
        int executeCallCountInteger = (Integer) callCountMap.get(COUNT_MAP_EXECUTE_CALL_COUNT);
        callCountMap.compute(COUNT_MAP_EXECUTE_CALL_COUNT, (key, value) -> executeCallCountInteger + 1);
    }

    @Override
    public void rollback(Database database) throws CustomChangeException, RollbackImpossibleException {
        Scope.getCurrentScope().getLog(getClass()).info("Goodbye " + getHelloTo());
        Map<String, Object> callCountMap = Scope.getCurrentScope().get(SCOPE_ATTR_CALL_COUNT_MAP, Map.class);
        if (callCountMap == null) {
            return;
        }
        int rollbackCallCount = (Integer) callCountMap.get(COUNT_MAP_ROLLBACK_CALL_COUNT);
        callCountMap.compute(COUNT_MAP_ROLLBACK_CALL_COUNT, (key, value) -> rollbackCallCount + 1);
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
