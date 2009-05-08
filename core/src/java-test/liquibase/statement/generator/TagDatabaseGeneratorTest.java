package liquibase.statement.generator;

public class TagDatabaseGeneratorTest {
////    @Test
////    public void isValidGenerator() throws Exception {
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
//                        new Liquibase("changelogs/common/common.tests.changelog.xml", new JUnitFileOpener(), database).update(null);
//                    }
//
//                    protected void preExecuteAssert(DatabaseSnapshot snapshot) throws JDBCException {
//                        assertFalse(snapshot.getDatabase().doesTagExist("TAG_NAME"));
//                    }
//
//                    protected void postExecuteAssert(DatabaseSnapshot snapshot) throws JDBCException {
//                        assertTrue(snapshot.getDatabase().doesTagExist("TAG_NAME"));
//                    }
//
//                });
//    }

}
