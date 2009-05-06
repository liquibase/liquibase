package liquibase.statement;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.exception.JDBCException;
import liquibase.lock.LockHandler;
import liquibase.test.DatabaseTestTemplate;
import liquibase.test.JUnitFileOpener;
import liquibase.test.SqlStatementDatabaseTest;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class TagDatabaseGeneratorTest extends AbstractSqStatementTest {

    protected void setupDatabase(Database database) throws Exception {
        new Liquibase(null, null, database).dropAll();
        LockHandler.getInstance(database).reset();
    }

    protected SqlStatement createGeneratorUnderTest() {
        return new TagDatabaseStatement(null);
    }

//    @Test
//    public void isValidGenerator() throws Exception {
//        new DatabaseTestTemplate().testOnAllDatabases(new DatabaseTest() {
//            public void performTest(Database database) throws Exception {
//                assertTrue(createGeneratorUnderTest().supportsDatabase(database));
//            }
//        });
//    }

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