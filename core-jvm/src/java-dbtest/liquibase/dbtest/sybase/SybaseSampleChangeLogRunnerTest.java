package liquibase.dbtest.sybase;

@SuppressWarnings({"JUnitTestCaseWithNoTests"})
public class SybaseSampleChangeLogRunnerTest { //extends AbstractSimpleChangeLogRunnerTest {

//    public SybaseSampleChangeLogRunnerTest() throws Exception {
//        super("sybase", "com.sybase.jdbc3.jdbc.SybDriver", "jdbc:sybase:Tds:"+ InetAddress.getLocalHost().getHostName()+":5000/liquibase");
//    }
//
//
//    protected void setUp() throws Exception {
//        super.setUp();
//        connection.setAutoCommit(true);
//    }
//
//
//    protected void tearDown() throws Exception {
//        super.tearDown();
//    }

    protected boolean shouldRollBack() {
        return false;
    }
    
}
