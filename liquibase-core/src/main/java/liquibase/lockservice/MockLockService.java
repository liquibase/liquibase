package liquibase.lockservice;

import liquibase.database.Database;
import liquibase.database.core.MockDatabase;

public class MockLockService extends NoLockService {

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean supports(Database database) {
        return database instanceof MockDatabase;
    }

}
