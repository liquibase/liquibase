package liquibase.migrator.mysql;

import liquibase.migrator.AbstractSimpleChangeLogRunnerTest;

public class MySQLSampleChangeLogRunnerTest extends AbstractSimpleChangeLogRunnerTest {

    public MySQLSampleChangeLogRunnerTest() {
        super("changelogs/mysql.changelog.xml", "mysql-5.0.4", "com.mysql.jdbc.Driver", "jdbc:mysql://localhost/liquibase");
    }
}
