package liquibase.dbtest.oracle;

import liquibase.dbtest.AbstractSimpleChangeLogRunnerTest;

/**
 * create tablespace liquibase2 datafile 'C:\ORACLEXE\ORADATA\XE\LIQUIBASE2.DBF' SIZE 5M autoextend on next 5M
 */
@SuppressWarnings({"JUnitTestCaseWithNoTests"})
public class OracleSampleChangeLogRunnerTest extends AbstractSimpleChangeLogRunnerTest {

    public OracleSampleChangeLogRunnerTest() throws Exception {
        super("oracle", "jdbc:oracle:thin:@localhost/XE");
    }
}
