package liquibase.sdk.executor;

import liquibase.executor.LoggingExecutor;
import liquibase.sdk.database.MockDatabase;

import java.io.StringWriter;

public class MockExecutor extends LoggingExecutor {

    public MockExecutor() {
        super(null, new StringWriter(), new MockDatabase());
    }

    public String getRanSql() {
        return getOutput().toString();
    }
}
