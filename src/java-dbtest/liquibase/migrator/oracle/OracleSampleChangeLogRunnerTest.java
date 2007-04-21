package liquibase.migrator.oracle;

import liquibase.migrator.AbstractSimpleChangeLogRunnerTest;

public class OracleSampleChangeLogRunnerTest extends AbstractSimpleChangeLogRunnerTest {

    public OracleSampleChangeLogRunnerTest() {
        super("changelogs/oracle.changelog.xml", "oracle-10.2", "oracle.jdbc.OracleDriver", "jdbc:oracle:thin:@localhost/XE");
    }
}
