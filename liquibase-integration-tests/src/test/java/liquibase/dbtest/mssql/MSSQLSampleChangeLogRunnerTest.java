package liquibase.dbtest.mssql;

import liquibase.dbtest.AbstractSimpleChangeLogRunnerTest;

public class MSSQLSampleChangeLogRunnerTest extends AbstractSimpleChangeLogRunnerTest {

    public MSSQLSampleChangeLogRunnerTest() throws Exception {
        super("mssql", "jdbc:sqlserver://localhost;instanceName=SQL2005;databaseName=liquibase");
    }
}
