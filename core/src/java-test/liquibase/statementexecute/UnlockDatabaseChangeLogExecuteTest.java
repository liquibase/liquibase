package liquibase.statementexecute;

import liquibase.statement.SqlStatement;
import liquibase.statement.CreateDatabaseChangeLogLockTableStatement;
import liquibase.statement.UnlockDatabaseChangeLogStatement;
import liquibase.database.Database;
import liquibase.database.MSSQLDatabase;

import java.util.List;
import java.util.Arrays;

import org.junit.Test;

public class UnlockDatabaseChangeLogExecuteTest extends AbstractExecuteTest {
    protected List<? extends SqlStatement> setupStatements(Database database) {
        return Arrays.asList(new CreateDatabaseChangeLogLockTableStatement());
    }
    
    @Test
    public void generateSql() throws Exception {
        this.statementUnderTest = new UnlockDatabaseChangeLogStatement();
        assertCorrect("update [dbo].[databasechangeloglock] set [locked] = FALSE, [lockedby] = null, [lockgranted] = null where  id = 1", MSSQLDatabase.class);
        assertCorrectOnRest("update [databasechangeloglock] set [locked] = FALSE, [lockedby] = null, [lockgranted] = null where  id = 1");
    }
}
