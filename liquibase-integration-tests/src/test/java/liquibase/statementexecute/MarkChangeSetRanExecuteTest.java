package liquibase.statementexecute;

import liquibase.statement.SqlStatement;
import liquibase.statement.core.CreateDatabaseChangeLogLockTableStatement;
import liquibase.statement.core.UnlockDatabaseChangeLogStatement;
import liquibase.statement.core.MarkChangeSetRanStatement;
import liquibase.statement.core.CreateDatabaseChangeLogTableStatement;
import liquibase.database.Database;
import liquibase.database.core.CacheDatabase;
import liquibase.database.core.DB2Database;
import liquibase.database.core.DerbyDatabase;
import liquibase.database.core.FirebirdDatabase;
import liquibase.database.core.H2Database;
import liquibase.database.core.HsqlDatabase;
import liquibase.database.core.InformixDatabase;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.MaxDBDatabase;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.database.core.SybaseASADatabase;
import liquibase.database.core.SybaseDatabase;
import liquibase.changelog.ChangeSet;
import liquibase.util.LiquibaseUtil;

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
        this.statementUnderTest = new MarkChangeSetRanStatement(new ChangeSet("a", "b", false, false, "d", "e", "f"), ChangeSet.ExecType.EXECUTED);
        String version = LiquibaseUtil.getBuildVersion().replaceAll("SNAPSHOT", "SNP");
        assertCorrect("insert into [dbo].[databasechangelog] ([author], [comments], [dateexecuted], [description], [exectype], [filename], [id], [liquibase], [md5sum], [orderexecuted]) values ('b', '', getdate(), 'empty', 'executed', 'c', 'a', '" + version + "', '3:d41d8cd98f00b204e9800998ecf8427e', 1)", MSSQLDatabase.class);
        assertCorrect("insert into [databasechangelog] ([author], [comments], [dateexecuted], [description], [exectype], [filename], [id], [liquibase], [md5sum], [orderexecuted]) values ('b', '', timestamp, 'empty', 'executed', 'c', 'a', '" + version + "', '3:d41d8cd98f00b204e9800998ecf8427e', 1)",MaxDBDatabase.class);
        assertCorrect("insert into [databasechangelog] ([author], [comments], [dateexecuted], [description], [exectype], [filename], [id], [liquibase], [md5sum], [orderexecuted]) values ('b', '', sysdate, 'empty', 'executed', 'c', 'a', '" + version + "', '3:d41d8cd98f00b204e9800998ecf8427e', 1)",CacheDatabase.class);
        assertCorrect("insert into [databasechangelog] ([author], [comments], [dateexecuted], [description], [exectype], [filename], [id], [liquibase], [md5sum], [orderexecuted]) values ('b', '', SYSTIMESTAMP, 'empty', 'executed', 'c', 'a', '" + version + "', '3:d41d8cd98f00b204e9800998ecf8427e', 1)",OracleDatabase.class);
        assertCorrect("insert into [databasechangelog] ([author], [comments], [dateexecuted], [description], [exectype], [filename], [id], [liquibase], [md5sum], [orderexecuted]) values ('b', '', getdate(), 'empty', 'executed', 'c', 'a', '" + version + "', '3:d41d8cd98f00b204e9800998ecf8427e', 1)",SybaseDatabase.class);
        assertCorrect("insert into [databasechangelog] ([author], [comments], [dateexecuted], [description], [exectype], [filename], [id], [liquibase], [md5sum], [orderexecuted]) values ('b', '', current year to fraction(5), 'empty', 'executed', 'c', 'a', '" + version + "', '3:d41d8cd98f00b204e9800998ecf8427e', 1)",InformixDatabase.class);
        assertCorrect("insert into [databasechangelog] ([author], [comments], [dateexecuted], [description], [exectype], [filename], [id], [liquibase], [md5sum], [orderexecuted]) values ('b', '', current timestamp, 'empty', 'executed', 'c', 'a', '" + version + "', '3:d41d8cd98f00b204e9800998ecf8427e', 1)",DB2Database.class);
        assertCorrect("insert into [databasechangelog] ([author], [comments], [dateexecuted], [description], [exectype], [filename], [id], [liquibase], [md5sum], [orderexecuted]) values ('b', '', current_timestamp, 'empty', 'executed', 'c', 'a', '" + version + "', '3:d41d8cd98f00b204e9800998ecf8427e', 1)",FirebirdDatabase.class, DerbyDatabase.class);
        assertCorrect("insert into [databasechangelog] ([author], [comments], [dateexecuted], [description], [exectype], [filename], [id], [liquibase], [md5sum], [orderexecuted]) values ('b', '', now, 'empty', 'executed', 'c', 'a', '" + version + "', '3:d41d8cd98f00b204e9800998ecf8427e', 1)",HsqlDatabase.class);
        assertCorrect("insert into [databasechangelog] ([author], [comments], [dateexecuted], [description], [exectype], [filename], [id], [liquibase], [md5sum], [orderexecuted]) values ('b', '', now(), 'empty', 'executed', 'c', 'a', '" + version + "', '3:d41d8cd98f00b204e9800998ecf8427e', 1)",MySQLDatabase.class,PostgresDatabase.class,SybaseASADatabase.class,H2Database.class);
        assertCorrectOnRest("insert into [databasechangelog] ([author], [comments], [dateexecuted], [description], [exectype], [filename], [id], [liquibase], [md5sum], [orderexecuted]) values ('b', '', now(), 'empty', 'executed', 'c', 'a', '" + version + "', '3:d41d8cd98f00b204e9800998ecf8427e', 1)");
    }

    @Test
    public void generateSql_update() throws Exception {
        this.statementUnderTest = new MarkChangeSetRanStatement(new ChangeSet("a", "b", false, false, "d", "e", "f"), ChangeSet.ExecType.RERAN);
        assertCorrect("update [dbo].[databasechangelog] set [dateexecuted] = NOW(), [exectype] = 'reran', [md5sum] = '3:d41d8cd98f00b204e9800998ecf8427e' where id='a' and author='b' and filename='c'", MSSQLDatabase.class);
        assertCorrect("update [databasechangelog] set [dateexecuted] = timestamp, [exectype] = 'reran', [md5sum] = '3:d41d8cd98f00b204e9800998ecf8427e' where id='a' and author='b' and filename='c'", MaxDBDatabase.class);
        assertCorrect("update [databasechangelog] set [dateexecuted] = sysdate, [exectype] = 'reran', [md5sum] = '3:d41d8cd98f00b204e9800998ecf8427e' where id='a' and author='b' and filename='c'", CacheDatabase.class);
        assertCorrect("update [databasechangelog] set [dateexecuted] = SYSTIMESTAMP, [exectype] = 'reran', [md5sum] = '3:d41d8cd98f00b204e9800998ecf8427e' where id='a' and author='b' and filename='c'", OracleDatabase.class);
        assertCorrect("update [databasechangelog] set [dateexecuted] = getdate(), [exectype] = 'reran', [md5sum] = '3:d41d8cd98f00b204e9800998ecf8427e' where id='a' and author='b' and filename='c'", SybaseDatabase.class);
        assertCorrect("update [databasechangelog] set [dateexecuted] = current year to fraction(5), [exectype] = 'reran', [md5sum] = '3:d41d8cd98f00b204e9800998ecf8427e' where id='a' and author='b' and filename='c'", InformixDatabase.class);
        assertCorrect("update [databasechangelog] set [dateexecuted] = current timestamp, [exectype] = 'reran', [md5sum] = '3:d41d8cd98f00b204e9800998ecf8427e' where id='a' and author='b' and filename='c'", DB2Database.class);
        assertCorrect("update [databasechangelog] set [dateexecuted] = current_timestamp, [exectype] = 'reran', [md5sum] = '3:d41d8cd98f00b204e9800998ecf8427e' where id='a' and author='b' and filename='c'", FirebirdDatabase.class, DerbyDatabase.class);
        assertCorrect("update [databasechangelog] set [dateexecuted] = NOW(), [exectype] = 'reran', [md5sum] = '3:d41d8cd98f00b204e9800998ecf8427e' where id='a' and author='b' and filename='c'", MySQLDatabase.class,HsqlDatabase.class,PostgresDatabase.class,SybaseASADatabase.class,H2Database.class);
        assertCorrectOnRest("update [databasechangelog] set [dateexecuted] = NOW(), [exectype] = 'reran', [md5sum] = '3:d41d8cd98f00b204e9800998ecf8427e' where id='a' and author='b' and filename='c'");
    }
}