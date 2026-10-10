package liquibase.sqlgenerator.core;

import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.database.core.HsqlDatabase;
import liquibase.database.core.InformixDatabase;
import liquibase.database.core.MSSQLDatabase;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.core.TagDatabaseStatement;
import org.junit.Test;

import java.util.Collections;

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
    public void testMSSQL_defaultDoesNotClearDuplicateTags() throws Exception {
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
    public void testMSSQL_clearDuplicateTagsEnabled() throws Exception {
        Scope.child(Collections.singletonMap(GlobalConfiguration.CLEAR_DUPLICATE_TAGS.getKey(), "true"), () -> {
            TagDatabaseStatement statement = new TagDatabaseStatement("v1.0");
            Sql[] sql = SqlGeneratorFactory.getInstance().generateSql(statement, new MSSQLDatabase());

            assertEquals(2, sql.length);
            assertEquals(
                    "UPDATE DATABASECHANGELOG SET TAG = NULL WHERE TAG = 'v1.0'",
                    sql[0].toSql());
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
                    sql[1].toSql());
        });
    }

    @Test
    public void testInformix_selectsOneActualLatestRow() throws Exception {
        // Independent MAX(DATEEXECUTED) / MAX(ORDEREXECUTED) can each come from a different row when they
        // don't tie together; FIRST 1 with an explicit ORDER BY always identifies one real row.
        Scope.child(Collections.singletonMap(GlobalConfiguration.CLEAR_DUPLICATE_TAGS.getKey(), "true"), () -> {
            TagDatabaseStatement statement = new TagDatabaseStatement("v1.0");
            Sql[] sql = SqlGeneratorFactory.getInstance().generateSql(statement, new InformixDatabase());

            assertEquals(4, sql.length);
            assertEquals(
                    "UPDATE DATABASECHANGELOG SET TAG = NULL WHERE TAG = 'v1.0'",
                    sql[0].toSql());
            assertEquals(
                    "SELECT FIRST 1 DATEEXECUTED, ORDEREXECUTED " +
                    "FROM DATABASECHANGELOG " +
                    "ORDER BY DATEEXECUTED DESC, ORDEREXECUTED DESC " +
                    "INTO TEMP max_order_temp WITH NO LOG",
                    sql[1].toSql());
            assertEquals(
                    "UPDATE DATABASECHANGELOG " +
                    "SET TAG = 'v1.0' " +
                    "WHERE DATEEXECUTED = (" +
                        "SELECT DATEEXECUTED " +
                        "FROM max_order_temp" +
                    ") AND ORDEREXECUTED = (" +
                        "SELECT ORDEREXECUTED " +
                        "FROM max_order_temp" +
                    ");",
                    sql[2].toSql());
            assertEquals("DROP TABLE max_order_temp;", sql[3].toSql());
        });
    }

    @Test
    public void testHsql_defaultDoesNotClearDuplicateTags() throws Exception {
        TagDatabaseStatement statement = new TagDatabaseStatement("v1.0");
        Sql[] sql = SqlGeneratorFactory.getInstance().generateSql(statement, new HsqlDatabase());

        assertEquals(1, sql.length);
        assertEquals(
                "UPDATE DATABASECHANGELOG " +
                "SET TAG = 'v1.0' " +
                "WHERE DATEEXECUTED = (" +
                    "SELECT MAX(DATEEXECUTED) " +
                    "FROM DATABASECHANGELOG" +
                ")",
                sql[0].toSql());
    }

    @Test
    public void testHsql_clearDuplicateTagsEnabled() throws Exception {
        Scope.child(Collections.singletonMap(GlobalConfiguration.CLEAR_DUPLICATE_TAGS.getKey(), "true"), () -> {
            TagDatabaseStatement statement = new TagDatabaseStatement("v1.0");
            Sql[] sql = SqlGeneratorFactory.getInstance().generateSql(statement, new HsqlDatabase());

            assertEquals(2, sql.length);
            assertEquals(
                    "UPDATE DATABASECHANGELOG SET TAG = NULL WHERE TAG = 'v1.0'",
                    sql[0].toSql());
            assertEquals(
                    "UPDATE DATABASECHANGELOG " +
                    "SET TAG = 'v1.0' " +
                    "WHERE DATEEXECUTED = (" +
                        "SELECT MAX(DATEEXECUTED) " +
                        "FROM DATABASECHANGELOG" +
                    ")",
                    sql[1].toSql());
        });
    }

}
