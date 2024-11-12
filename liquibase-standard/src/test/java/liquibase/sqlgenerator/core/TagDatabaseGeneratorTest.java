package liquibase.sqlgenerator.core;

import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.DatabaseConnection;
import liquibase.database.core.MSSQLDatabase;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.core.TagDatabaseStatement;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TagDatabaseGeneratorTest {
////    @Test
////    public void supports() throws Exception {
////        new DatabaseTestTemplate().testOnAllDatabases(new DatabaseTest() {
////            public void performTest(Database database) throws Exception {
////                assertTrue(createGeneratorUnderTest().supportsDatabase(database));
////            }
////        });
////    }
//
//    @Test
//    public void execute() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new SqlStatementDatabaseTest(null, new TagDatabaseStatement("TAG_NAME")) {
//                    protected void setup(Database database) throws Exception {
//                        new Liquibase("changelogs/common/common.tests.changelog.xml", new JUnitResourceAccessor(), database).update(null);
//                    }
//
//                    protected void preExecuteAssert(DatabaseSnapshotGenerator snapshot) throws DatabaseException {
//                        assertFalse(snapshot.getDatabase().doesTagExist("TAG_NAME"));
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshotGenerator snapshot) throws DatabaseException {
//                        assertTrue(snapshot.getDatabase().doesTagExist("TAG_NAME"));
//                    }
//
//                });
//    }

    @Test
    public void testMSSQL() {
        TagDatabaseStatement statement = new TagDatabaseStatement("v1.0");
        Sql[] sql = SqlGeneratorFactory.getInstance().generateSql(statement, new MSSQLDatabase());

        assertEquals(1, sql.length);
        assertEquals(
            "UPDATE changelog " +
                "SET TAG = 'v1.0' " +
                "FROM DATABASECHANGELOG AS changelog " +
                "INNER JOIN (" +
                "SELECT TOP (1) ID, AUTHOR, FILENAME " +
                "FROM DATABASECHANGELOG " +
                "ORDER BY DATEEXECUTED DESC, ORDEREXECUTED DESC" +
                ") AS latest " +
                "ON latest.ID = changelog.ID " +
                "AND latest.AUTHOR = changelog.AUTHOR " +
                "AND latest.FILENAME = changelog.FILENAME",
                sql[0].toSql());
    }

    @Test
    public void testOtherDb() {
        TagDatabaseStatement statement = new TagDatabaseStatement("v1.0");

        Sql[] sql = SqlGeneratorFactory.getInstance().generateSql(statement, new OtherDb());

        assertEquals(1, sql.length);
        assertEquals(
                "UPDATE DATABASECHANGELOG " +
                "SET TAG = 'v1.0' " +
                "WHERE ORDEREXECUTED = (" +
                  "SELECT MAX(ORDEREXECUTED) FROM DATABASECHANGELOG " +
                  "WHERE DATEEXECUTED = (" +
                    "SELECT MAX(DATEEXECUTED) FROM DATABASECHANGELOG" +
                  ")" +
                ")",
                sql[0].toSql());
    }

    private static class OtherDb extends AbstractJdbcDatabase {

        @Override
        protected String getDefaultDatabaseProductName() {
            return null;
        }

        @Override
        public boolean isCorrectDatabaseImplementation(final DatabaseConnection conn) {
            return false;
        }

        @Override
        public String getDefaultDriver(final String url) {
            return null;
        }

        @Override
        public String getShortName() {
            return "OtherDb";
        }

        @Override
        public Integer getDefaultPort() {
            return null;
        }

        @Override
        public boolean supportsInitiallyDeferrableColumns() {
            return false;
        }

        @Override
        public boolean supportsTablespaces() {
            return false;
        }

        @Override
        public int getPriority() {
            return 0;
        }
    }
}
