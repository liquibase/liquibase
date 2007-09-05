package liquibase.migrator.oracle;

import liquibase.migrator.AbstractSimpleChangeLogRunnerTest;

/**
 * create tablespace liquibase2 datafile 'C:\ORACLEXE\ORADATA\XE\LIQUIBASE2.DBF' SIZE 5M autoextend on next 5M
 */
@SuppressWarnings({"JUnitTestCaseWithNoTests"})
public class OracleSampleChangeLogRunnerTest extends AbstractSimpleChangeLogRunnerTest {

    public OracleSampleChangeLogRunnerTest() throws Exception {
        super("oracle", "oracle.jdbc.OracleDriver", "jdbc:oracle:thin:@localhost/XE");
    }
}
