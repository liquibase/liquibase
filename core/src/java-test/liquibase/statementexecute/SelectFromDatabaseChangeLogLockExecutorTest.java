package liquibase.statementexecute;

import liquibase.database.Database;
import liquibase.database.MSSQLDatabase;
import liquibase.statement.SqlStatement;
import liquibase.statement.CreateDatabaseChangeLogLockTableStatement;
import liquibase.statement.SelectFromDatabaseChangeLogLockStatement;

import java.util.List;
import java.util.Arrays;

import org.junit.Test;

public class SelectFromDatabaseChangeLogLockExecutorTest extends AbstractExecuteTest {

    protected List<? extends SqlStatement> setupStatements(Database database) {
        return Arrays.asList(new CreateDatabaseChangeLogLockTableStatement());
    }

    @Test
    public void generateSql() throws Exception {
        this.statementUnderTest = new SelectFromDatabaseChangeLogLockStatement("LOCKED");
        assertCorrect("select locked from [dbo].[databasechangeloglock] where [id]=1", MSSQLDatabase.class);
        assertCorrect("select locked from [databasechangeloglock] where [id]=1");
    }

}
