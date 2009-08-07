package liquibase.statementexecute;

import liquibase.statement.SqlStatement;
import liquibase.statement.core.CreateDatabaseChangeLogLockTableStatement;
import liquibase.statement.core.UnlockDatabaseChangeLogStatement;
import liquibase.statement.core.MarkChangeSetRanStatement;
import liquibase.statement.core.CreateDatabaseChangeLogTableStatement;
import liquibase.database.Database;
import liquibase.database.core.H2Database;
import liquibase.database.core.HsqlDatabase;
import liquibase.database.core.InformixDatabase;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.MaxDBDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.changelog.ChangeSet;

import java.util.List;
import java.util.Arrays;

import org.junit.Test;

public class MarkChangeSetRanExecuteTest extends AbstractExecuteTest {
    @Override
    protected List<? extends SqlStatement> setupStatements(Database database) {
        return Arrays.asList(new CreateDatabaseChangeLogTableStatement());
    }

    @Test
    public void generateSql_insert() throws Exception {
        this.statementUnderTest = new MarkChangeSetRanStatement(new ChangeSet("a", "b", false, false, "c", "d", "e", "f"), false);
        assertCorrect("insert into [dbo].[databasechangelog] ([liquibase], [md5sum], [dateexecuted], [orderexecuted], [author], [comments], [filename], [description], [id]) values ('2.0.b0', '2:d41d8cd98f00b204e9800998ecf8427e', NOW(), 1, 'b', '', 'c', 'empty', 'a')", MSSQLDatabase.class);
        assertCorrectOnRest("insert into [databasechangelog] ([liquibase], [md5sum], [dateexecuted], [orderexecuted], [author], [comments], [filename], [description], [id]) values ('2.0.b0', '2:d41d8cd98f00b204e9800998ecf8427e', NOW(), 1, 'b', '', 'c', 'empty', 'a')");
    }

    @Test
    public void generateSql_update() throws Exception {
        this.statementUnderTest = new MarkChangeSetRanStatement(new ChangeSet("a", "b", false, false, "c", "d", "e", "f"), true);
        assertCorrect("update [dbo].[databasechangelog] set [dateexecuted] = NOW(), [md5sum] = '2:d41d8cd98f00b204e9800998ecf8427e' where id='a' and author='b' and filename='c'", MSSQLDatabase.class);
        assertCorrectOnRest("update [databasechangelog] set [dateexecuted] = NOW(), [md5sum] = '2:d41d8cd98f00b204e9800998ecf8427e' where id='a' and author='b' and filename='c'");
    }
}