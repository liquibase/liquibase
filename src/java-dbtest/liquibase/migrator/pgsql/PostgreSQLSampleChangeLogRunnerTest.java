package liquibase.migrator.pgsql;

import liquibase.migrator.AbstractSimpleChangeLogRunnerTest;

public class PostgreSQLSampleChangeLogRunnerTest extends AbstractSimpleChangeLogRunnerTest {

    public PostgreSQLSampleChangeLogRunnerTest() {
        super("changelogs/pgsql.changelog.xml", "postgres-8.2", "org.postgresql.Driver", "jdbc:postgresql://localhost/liquibase");
    }
}
