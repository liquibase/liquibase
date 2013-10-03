package liquibase.dbtest.sybase;

public abstract class SybaseJtdsIntegrationTest { // extends AbstractIntegrationTest {

//    public MssqlJtdsIntegrationTest() throws Exception {
//        super("sybase", "net.sourceforge.jtds.jdbc.Driver", "jdbc:jtds:sybase://localhost/nathan:5000");
//    }
//
//    protected void setUp() throws Exception {
//        super.setUp();
//        connection.setAutoCommit(true);
//    }

    protected boolean shouldRollBack() {
        return false;
    }

}
