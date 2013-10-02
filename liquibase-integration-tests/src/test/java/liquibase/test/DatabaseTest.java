package liquibase.test;

import liquibase.database.Database;

public interface DatabaseTest {
    public void performTest(Database database) throws Exception;
}
