package liquibase.test;

import liquibase.database.Database;
import liquibase.exception.JDBCException;
import org.junit.ComparisonFailure;

import java.util.Set;

public class DatabaseTestTemplate {
    public void testOnAvailableDatabases(DatabaseTest test) throws Exception {
        test(test, TestContext.getInstance().getAvailableDatabases());
    }

    public void testOnAllDatabases(DatabaseTest test) throws Exception {
        test(test, TestContext.getInstance().getAllDatabases());
    }

    private void test(DatabaseTest test, Set<Database> databasesToTestOn) throws Exception {
        for (Database database : databasesToTestOn) {
            try {
                test.performTest(database);
            } catch (ComparisonFailure e) {
                String newMessage = "Database Test Failure on " + database;
                if (e.getMessage() != null) {
                    newMessage += ": " + e.getMessage();
                }

                ComparisonFailure newError = new ComparisonFailure(newMessage, e.getExpected(), e.getActual());
                newError.setStackTrace(e.getStackTrace());
                throw newError;
            } catch (AssertionError e) {
                String newMessage = "Database Test Failure on " + database;
                if (e.getMessage() != null) {
                    newMessage += ": " + e.getMessage();
                }

                AssertionError newError = new AssertionError(newMessage);
                newError.setStackTrace(e.getStackTrace());
                throw newError;
            }
        }
    }
}
