package liquibase.context;

import org.junit.Test;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertSame;

public class ExecutionContextTest {

    @Test
    public void getContext_defaultSetup() {
        ExecutionContext executionContext = new ExecutionContext(new SystemPropertyValueContainer());
        GlobalContext globalContext = executionContext.getContext(GlobalContext.class);

        assertNotNull(globalContext);

        assertSame("Multiple calls to getContext should return the same instance", globalContext, executionContext.getContext(GlobalContext.class));
    }
}
