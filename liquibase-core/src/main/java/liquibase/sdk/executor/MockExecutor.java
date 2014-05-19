package liquibase.sdk.executor;

import liquibase.executor.LoggingExecutor;
import liquibase.sdk.database.MockDatabase;
import liquibase.servicelocator.LiquibaseService;

import java.io.StringWriter;

@LiquibaseService(skip=true)
public class MockExecutor extends LoggingExecutor {

    public MockExecutor() {
        super(null, new StringWriter(), new MockDatabase());
    }

    public String getRanSql() {
        return getOutput().toString();
    }
}
