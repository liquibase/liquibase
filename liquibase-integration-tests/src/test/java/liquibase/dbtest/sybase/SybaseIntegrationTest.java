package liquibase.dbtest.sybase;

public abstract class SybaseIntegrationTest { //extends AbstractIntegrationTest {

//    public SybaseIntegrationTest() throws Exception {
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
