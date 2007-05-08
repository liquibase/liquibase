package liquibase.migrator.mysql;

import liquibase.migrator.AbstractSimpleChangeLogRunnerTest;

public class MySQLSampleChangeLogRunnerTest extends AbstractSimpleChangeLogRunnerTest {

    public MySQLSampleChangeLogRunnerTest() {
        super("mysql", "mysql-5.0.4", "com.mysql.jdbc.Driver", "jdbc:mysql://localhost/liquibase");
    }
}
