package liquibase.database.sql;

import liquibase.database.*;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.exception.JDBCException;
import liquibase.test.*;
import liquibase.Liquibase;
import liquibase.lock.LockHandler;
import static org.junit.Assert.*;
import org.junit.Test;

public class TagDatabaseStatementTest extends AbstractSqlStatementTest {

    protected void setupDatabase(Database database) throws Exception {
        new Liquibase(null, null, database).dropAll();
        LockHandler.getInstance(database).reset();
    }

    protected SqlStatement generateTestStatement() {
        return new TagDatabaseStatement(null);
    }

    @Test
    public void supportsDatabase() throws Exception {
        new DatabaseTestTemplate().testOnAllDatabases(new DatabaseTest() {
            public void performTest(Database database) throws Exception {
                assertTrue(generateTestStatement().supportsDatabase(database));
            }
        });
    }

    @Test
    public void execute() throws Exception {
        new DatabaseTestTemplate().testOnAvailableDatabases(
                new SqlStatementDatabaseTest(null, new TagDatabaseStatement("TAG_NAME")) {
                    protected void setup(Database database) throws Exception {
                        new Liquibase("changelogs/common/common.tests.changelog.xml", new JUnitFileOpener(), database).update(null);
                    }

                    protected void preExecuteAssert(DatabaseSnapshot snapshot) throws JDBCException {
                        assertFalse(snapshot.getDatabase().doesTagExist("TAG_NAME"));
                    }

                    protected void postExecuteAssert(DatabaseSnapshot snapshot) throws JDBCException {
                        assertTrue(snapshot.getDatabase().doesTagExist("TAG_NAME"));
                    }

                });
    }

}