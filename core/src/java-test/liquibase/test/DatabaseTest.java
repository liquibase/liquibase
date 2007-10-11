package liquibase.test;

import liquibase.database.Database;
import liquibase.exception.JDBCException;

public interface DatabaseTest {
    public void performTest(Database database) throws JDBCException;
}
