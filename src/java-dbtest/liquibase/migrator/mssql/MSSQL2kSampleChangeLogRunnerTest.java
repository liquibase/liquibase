package liquibase.migrator.mssql;

import liquibase.migrator.AbstractSimpleChangeLogRunnerTest;

public class MSSQL2kSampleChangeLogRunnerTest extends AbstractSimpleChangeLogRunnerTest {

    public MSSQL2kSampleChangeLogRunnerTest() {
//        super("changelogs/mssql.changelog.xml", "mssql-2005-1.0", "com.microsoft.sqlserver.jdbc.SQLServerDriver", "jdbc:sqlserver://windev1;instanceName=latest;databaseName=liquibase");
        super("changelogs/mssql.changelog.xml", "jtds-1.2", "net.sourceforge.jtds.jdbc.Driver", "jdbc:jtds:sqlserver://windev1.sundog.net;instance=latest;DatabaseName=liquibase");
        this.username="sundog";
        this.password = "sundog";
    }
}
