package liquibase.sqlgenerator;

import liquibase.database.Database;
import liquibase.database.PostgresDatabase;
import liquibase.database.MySQLDatabase;
import liquibase.database.MSSQLDatabase;
import liquibase.statement.*;

import java.util.List;
import java.util.Arrays;

import org.junit.Test;

public class SelectFromDatabaseChangeLogLockGeneratorTest<T extends SelectFromDatabaseChangeLogLockStatement> extends AbstractSqlGeneratorTest<T> {
    public SelectFromDatabaseChangeLogLockGeneratorTest() throws Exception {
        super(new SelectFromDatabaseChangeLogLockGenerator());
    }

    protected T createSampleSqlStatement() {
        return (T) new SelectFromDatabaseChangeLogLockStatement("LOCKED");
    }

    protected List<? extends SqlStatement> setupStatements(Database database) {
        return Arrays.asList(new CreateDatabaseChangeLogLockTableStatement());
    }

    @Test
    public void generateSql() throws Exception {
        SelectFromDatabaseChangeLogLockStatement statement = new SelectFromDatabaseChangeLogLockStatement("LOCKED");
        testSqlOnAllExcept("select locked from [databasechangeloglock] where [id]=1", (T) statement, MSSQLDatabase.class);
        testSqlOn("select locked from [dbo].[databasechangeloglock] where [id]=1", (T) statement, MSSQLDatabase.class);
    }
}
