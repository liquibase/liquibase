package liquibase;

import liquibase.test.TestContext;

import java.util.HashMap;
import java.util.Map;

public class TestScopeManager extends SingletonScopeManager {

    public TestScopeManager() {

    }

    @Override
    protected Scope init(Scope scope) throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put(Scope.Attr.resourceAccessor.name(), TestContext.getInstance().getTestResourceAccessor());
        return new Scope(scope, data);
    }
}
