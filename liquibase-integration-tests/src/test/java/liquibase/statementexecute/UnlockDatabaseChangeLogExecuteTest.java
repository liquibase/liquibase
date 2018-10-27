package liquibase.statementexecute;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import liquibase.database.Database;
import liquibase.database.core.H2Database;
import liquibase.database.core.HsqlDatabase;
import liquibase.database.core.InformixDatabase;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.database.core.SybaseASADatabase;
import liquibase.database.core.SybaseDatabase;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.CreateDatabaseChangeLogLockTableStatement;
import liquibase.statement.core.UnlockDatabaseChangeLogStatement;

public class UnlockDatabaseChangeLogExecuteTest extends AbstractExecuteTest {
    private static final String LOCKED_BY_ID = "9e5f4429-4feb-4cd4-a0b8-c521d9846b4c";

    @Override
    protected List<? extends SqlStatement> setupStatements(Database database) {
        return Arrays.asList(new CreateDatabaseChangeLogLockTableStatement());
    }
    
    @Test
    public void generateSql() throws Exception {
        this.statementUnderTest = new UnlockDatabaseChangeLogStatement(LOCKED_BY_ID);
        assertCorrect("update [databasechangeloglock] " +
            "set [locked] = 0, [lockedby] = null, [lockedbyid] = null, [lockexpires] = null, [lockgranted] = null " +
            "where [id] = 1 and lockedbyid  = '9e5f4429-4feb-4cd4-a0b8-c521d9846b4c'", MSSQLDatabase.class, SybaseDatabase.class);

        assertCorrect("update [databasechangeloglock] set [locked] = 0, [lockedby] = null, [lockedbyid] = null, [lockexpires] = null, [lockgranted] = null where [id] = 1 and lockedbyid  = '9e5f4429-4feb-4cd4-a0b8-c521d9846b4c", MSSQLDatabase.class, SybaseASADatabase.class);
        assertCorrect("update [databasechangeloglock] set [locked] = 'f', [lockedby] = null, [lockedbyid] = null, [lockexpires] = null, [lockgranted] = null where [id] = 1 and lockedbyid  = '9e5f4429-4feb-4cd4-a0b8-c521d9846b4c", InformixDatabase.class);
        assertCorrect("update [databasechangeloglock] set [locked] = false, [lockedby] = null, [lockedbyid] = null, [lockexpires] = null, [lockgranted] = null where [id] = 1 and lockedbyid  = '9e5f4429-4feb-4cd4-a0b8-c521d9846b4c", PostgresDatabase.class, HsqlDatabase.class, H2Database.class);
        assertCorrectOnRest("update [databasechangeloglock] set [locked] = 0, [lockedby] = null, [lockedbyid] = null, [lockexpires] = null, [lockgranted] = null where [id] = 1 and lockedbyid  = '9e5f4429-4feb-4cd4-a0b8-c521d9846b4c");
    }
}
