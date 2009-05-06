package liquibase.dbtest.hibernate;

public class HibernateTest {
    private static final String HIBERNATE_CONFIG_FILE = "Hibernate.cfg.xml";

//    @Test
//    public void runGeneratedChangeLog() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new DatabaseTest() {
//                    public void performTest(Database database) throws Exception {
//                        if (!(database instanceof MySQLDatabase)) {
//                            return;
//                        }
//                        Liquibase liquibase = new Liquibase(null, new JUnitFileOpener(), database);
//                        liquibase.dropAll();
//
//                        Database hibernateDatabase = new HibernateDatabase(HIBERNATE_CONFIG_FILE);
//
//                        Diff diff = new Diff(hibernateDatabase, database);
//                        DiffResult diffResult = diff.compare();
//
//                        assertTrue(diffResult.getMissingTables().size() > 0);
//
//                        File outFile = File.createTempFile("lb-test", "xml");
//                        OutputStream outChangeLog = new FileOutputStream(outFile);
//                        diffResult.printChangeLog(new PrintStream(outChangeLog), hibernateDatabase);
//                        outChangeLog.close();
//
//                        liquibase = new Liquibase(outFile.getName(), new JUnitFileOpener(), database);
//                        liquibase.update(null);
//
//                        diff = new Diff(hibernateDatabase, database);
//                        diffResult = diff.compare();
//
//                        assertEquals(0, diffResult.getMissingTables().size());
//                        assertEquals(0, diffResult.getMissingColumns().size());
//                        assertEquals(0, diffResult.getMissingPrimaryKeys().size());
//                        assertEquals(0, diffResult.getMissingIndexes().size());
//                        assertEquals(0, diffResult.getMissingViews().size());
//
//                        assertEquals(0, diffResult.getUnexpectedTables().size());
//                        assertEquals(0, diffResult.getUnexpectedColumns().size());
//                        assertEquals(0, diffResult.getUnexpectedPrimaryKeys().size());
//                        assertEquals(0, diffResult.getUnexpectedIndexes().size());
//                        assertEquals(0, diffResult.getUnexpectedViews().size());
//                    }
//                });
//    }
//
//    @Test
//    public void hibernateSchemaUpdate() throws Exception {
//        new DatabaseTestTemplate().testOnAvailableDatabases(
//                new DatabaseTest() {
//                    public void performTest(Database database) throws Exception {
//                        if (!(database instanceof MySQLDatabase)) {
//                            return;
//                        }
//
////                        Class.forName(database.getDriverName());
//                        SimpleNamingContextBuilder builder = SimpleNamingContextBuilder.emptyActivatedContextBuilder();
//                        Connection connection = database.getConnection().getUnderlyingConnection();
//                        SingleConnectionDataSource ds = new SingleConnectionDataSource(connection, true);
//                        builder.bind("java:/data", ds);
////                        builder.activate();
//
//                        Configuration cfg = new Configuration();
//                        cfg.configure(HIBERNATE_CONFIG_FILE);
//
//                        SchemaExport export = new SchemaExport(cfg);
//                        export.execute(false, true, false, false);
//
//                        connection.setAutoCommit(false);
//
//                        Database hibernateDatabase = new HibernateDatabase(HIBERNATE_CONFIG_FILE);
//
//                        Diff diff = new Diff(hibernateDatabase, database);
//                        DiffResult diffResult = diff.compare();
//
//                        assertEquals(0, diffResult.getMissingTables().size());
//                        assertEquals(0, diffResult.getMissingColumns().size());
//                        assertEquals(0, diffResult.getMissingPrimaryKeys().size());
//                        assertEquals(0, diffResult.getMissingIndexes().size());
//                        assertEquals(0, diffResult.getMissingViews().size());
//                        assertEquals(0, diffResult.getMissingForeignKeys().size());
//
//                        assertEquals(0, diffResult.getUnexpectedTables().size());
//                        assertEquals(0, diffResult.getUnexpectedColumns().size());
//                        assertEquals(0, diffResult.getUnexpectedPrimaryKeys().size());
//                        assertEquals(0, diffResult.getUnexpectedIndexes().size());
//                        assertEquals(0, diffResult.getUnexpectedViews().size());
//                        assertEquals(0, diffResult.getUnexpectedForeignKeys().size());
//                    }
//                });
//
//    }
}
