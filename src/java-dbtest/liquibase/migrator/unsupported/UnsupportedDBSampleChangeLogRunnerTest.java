package liquibase.migrator.unsupported;

/**
 * TODO: find a good way to implement that doesn't require wrapping the connection object.
 * Use to test using Derby, but changing the reported type so LiquiBase doesn't recognize it.
 */
@SuppressWarnings({"ALL"})
public class UnsupportedDBSampleChangeLogRunnerTest { // extends AbstractSimpleChangeLogRunnerTest {

//    public UnsupportedDBSampleChangeLogRunnerTest() throws Exception {
//        super("unsupported", "org.apache.derby.jdbc.EmbeddedDriver", "jdbc:derby:liquibase;create=true");
//    }
//
//    protected void setUp() throws Exception {
//        super.setUp();
////        connection = new ConnectionWrapper(connection);
//        System.out.println(connection.getClass().getName());
//    }
//
//
//    protected void tearDown() throws Exception {
//        try {
//            driver.connect("jdbc:derby:liquibase;shutdown=true", new Properties());
//        } catch (SQLException e) {
//            ;//clean shutdown throws exception.
//        }
//        super.tearDown();
//    }
//
//    protected Migrator createMigrator(String changeLogFile) throws Exception {
//        Migrator migrator = super.createMigrator(changeLogFile);
//        migrator.setCurrentDateTimeFunction("CURRENT_TIMESTAMP");
//        return migrator;
//    }
//
//    public void testIsActuallyUnsupportedDatabase() throws Exception {
//        Migrator migrator = createMigrator(null);
//
//        assertTrue("Not using unsupported database", migrator.getDatabase() instanceof UnsupportedDatabase);
//
//    }
//
//    public void testGetDefaultDriver() throws Exception {
//        assertNull(createMigrator(null).getDatabase().getDefaultDriver(url));
//    }
//
//    protected boolean shouldRollBack() {
//        return false;
//    }
}
