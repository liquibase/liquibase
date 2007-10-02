package liquibase.migrator.sybase;

@SuppressWarnings({"JUnitTestCaseWithNoTests"})
public class JTDSSampleChangeLogRunnerTest { // extends AbstractSimpleChangeLogRunnerTest {

//    public JTDSSampleChangeLogRunnerTest() throws Exception {
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
