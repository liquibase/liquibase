package liquibase.sqlgenerator.core;

public abstract class TagDatabaseGeneratorTest {
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

}
