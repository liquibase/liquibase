package liquibase.dbtest.mssql;

import liquibase.dbtest.AbstractSimpleChangeLogRunnerTest;

public class MSSQLSampleChangeLogRunnerTest extends AbstractSimpleChangeLogRunnerTest {

    public MSSQLSampleChangeLogRunnerTest() throws Exception {
        super("mssql", "jdbc:sqlserver://"+DATABASE_SERVER_HOSTNAME+";instanceName=SQL2005;databaseName=liquibase");
    }
}
