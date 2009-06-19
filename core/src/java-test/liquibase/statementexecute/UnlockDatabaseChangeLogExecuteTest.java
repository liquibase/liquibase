package liquibase.statementexecute;

import liquibase.statement.SqlStatement;
import liquibase.statement.CreateDatabaseChangeLogLockTableStatement;
import liquibase.statement.UnlockDatabaseChangeLogStatement;
import liquibase.database.Database;
import liquibase.database.core.H2Database;
import liquibase.database.core.HsqlDatabase;
import liquibase.database.core.InformixDatabase;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.MaxDBDatabase;
import liquibase.database.core.PostgresDatabase;

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
        assertCorrect("update [dbo].[databasechangeloglock] set [locked] = 0, [lockedby] = null, [lockgranted] = null where [id] = 1", MSSQLDatabase.class);
        assertCorrect("update [databasechangeloglock] set [locked] = 'f', [lockedby] = null, [lockgranted] = null where [id] = 1", InformixDatabase.class);
        assertCorrect("update [databasechangeloglock] set [locked] = false, [lockedby] = null, [lockgranted] = null where [id] = 1", PostgresDatabase.class, HsqlDatabase.class, H2Database.class, MaxDBDatabase.class);
        assertCorrectOnRest("update [databasechangeloglock] set [locked] = 0, [lockedby] = null, [lockgranted] = null where [id] = 1");
    }
}
