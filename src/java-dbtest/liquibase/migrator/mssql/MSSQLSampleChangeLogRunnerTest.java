package liquibase.migrator.mssql;

import liquibase.migrator.AbstractSimpleChangeLogRunnerTest;
import liquibase.migrator.JUnitFileOpener;
import liquibase.migrator.Migrator;
import liquibase.migrator.JUnitJDBCDriverClassLoader;

import java.sql.Driver;
import java.sql.Connection;
import java.util.Properties;
import java.util.Date;

public class MSSQLSampleChangeLogRunnerTest extends AbstractSimpleChangeLogRunnerTest {

    public MSSQLSampleChangeLogRunnerTest() {
        super("mssql", "mssql-2005-1.0", "com.microsoft.sqlserver.jdbc.SQLServerDriver", "jdbc:sqlserver://localhost;databaseName=liquibase");
    }
}
