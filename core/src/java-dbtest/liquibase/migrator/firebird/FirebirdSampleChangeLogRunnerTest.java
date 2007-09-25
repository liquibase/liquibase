package liquibase.migrator.firebird;

import liquibase.migrator.AbstractSimpleChangeLogRunnerTest;

/**
 * create tablespace liquibase2 datafile 'C:\ORACLEXE\ORADATA\XE\LIQUIBASE2.DBF' SIZE 5M autoextend on next 5M
 */
@SuppressWarnings({"JUnitTestCaseWithNoTests"})
public class FirebirdSampleChangeLogRunnerTest extends AbstractSimpleChangeLogRunnerTest {

    public FirebirdSampleChangeLogRunnerTest() throws Exception {
        super("firebird", "org.firebirdsql.jdbc.FBDriver", "jdbc:firebirdsql:localhost/3050:c:\\firebird\\liquibase.fdb");
    }
}
