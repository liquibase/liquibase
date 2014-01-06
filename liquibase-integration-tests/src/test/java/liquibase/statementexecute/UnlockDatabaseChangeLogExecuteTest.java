package liquibase.statementexecute;

import liquibase.database.core.*;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.CreateDatabaseChangeLogLockTableStatement;
import liquibase.statement.core.UnlockDatabaseChangeLogStatement;
import liquibase.database.Database;

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
        assertCorrect("update [databasechangeloglock] set [locked] = 0, [lockedby] = null, [lockgranted] = null where [id] = 1", MSSQLDatabase.class, SybaseDatabase.class);
        assertCorrect("update [databasechangeloglock] set [locked] = 0, [lockedby] = null, [lockgranted] = null where [id] = 1", MSSQLDatabase.class, SybaseASADatabase.class);
        assertCorrect("update [databasechangeloglock] set [locked] = 'f', [lockedby] = null, [lockgranted] = null where [id] = 1", InformixDatabase.class);
        assertCorrect("update [databasechangeloglock] set [locked] = false, [lockedby] = null, [lockgranted] = null where [id] = 1", PostgresDatabase.class, HsqlDatabase.class, H2Database.class);
        assertCorrectOnRest("update [databasechangeloglock] set [locked] = 0, [lockedby] = null, [lockgranted] = null where [id] = 1");
    }
}
