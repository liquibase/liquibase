package liquibase.statementexecute;

import liquibase.statement.SqlStatement;
import liquibase.statement.CreateDatabaseChangeLogLockTableStatement;
import liquibase.statement.UnlockDatabaseChangeLogStatement;
import liquibase.database.Database;
import liquibase.database.H2Database;
import liquibase.database.HsqlDatabase;
import liquibase.database.InformixDatabase;
import liquibase.database.MSSQLDatabase;
import liquibase.database.MaxDBDatabase;
import liquibase.database.PostgresDatabase;

import java.util.List;
import java.util.Arrays;

import org.junit.Test;

public class UnlockDatabaseChangeLogExecuteTest extends AbstractExecuteTest {
    @Override
    protected List<? extends SqlStatement> setupStatements(Database database) {
        return Arrays.asList(new CreateDatabaseChangeLogLockTableStatement());
    }
    
    @Test
    public void generateSql() throws Exception {
        this.statementUnderTest = new UnlockDatabaseChangeLogStatement();
        assertCorrect("update [dbo].[databasechangeloglock] set [lockedby] = null, [lockgranted] = null, [locked] = 0 where  id = 1", MSSQLDatabase.class);
        assertCorrect("update [databasechangeloglock] set [lockedby] = null, [lockgranted] = null, [locked] = 'f' where  id = 1", InformixDatabase.class);
        assertCorrect("update [databasechangeloglock] set [lockedby] = null, [lockgranted] = null, [locked] = false where  id = 1", PostgresDatabase.class, HsqlDatabase.class, H2Database.class, MaxDBDatabase.class);
        assertCorrectOnRest("update [databasechangeloglock] set [lockedby] = null, [lockgranted] = null, [locked] = 0 where  id = 1");
    }
}
