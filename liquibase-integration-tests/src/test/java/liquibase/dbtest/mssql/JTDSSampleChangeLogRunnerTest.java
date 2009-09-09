package liquibase.dbtest.mssql;

import liquibase.dbtest.AbstractSimpleChangeLogRunnerTest;

public class JTDSSampleChangeLogRunnerTest extends AbstractSimpleChangeLogRunnerTest {

    public JTDSSampleChangeLogRunnerTest() throws Exception {
        super("mssql", "jdbc:jtds:sqlserver://localhost;databaseName=liquibase");
    }
}
