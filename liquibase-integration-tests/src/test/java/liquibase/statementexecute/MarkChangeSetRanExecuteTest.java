package liquibase.statementexecute;

import liquibase.statement.SqlStatement;
import liquibase.statement.core.MarkChangeSetRanStatement;
import liquibase.statement.core.CreateDatabaseChangeLogTableStatement;
import liquibase.database.Database;
import liquibase.database.core.DB2Database;
import liquibase.database.core.DerbyDatabase;
import liquibase.database.core.FirebirdDatabase;
import liquibase.database.core.H2Database;
import liquibase.database.core.HsqlDatabase;
import liquibase.database.core.InformixDatabase;
import liquibase.database.core.MSSQLDatabase;
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
        this.statementUnderTest = new MarkChangeSetRanStatement(new ChangeSet("a", "b", false, false, "c", "e", "f", null), ChangeSet.ExecType.EXECUTED);
        String version = LiquibaseUtil.getBuildVersion().replaceAll("SNAPSHOT", "SNP");
        assertCorrect("insert into [databasechangelog] ([id], [author], [filename], [dateexecuted], [orderexecuted], [md5sum], [description], [comments], [exectype], [liquibase]) values ('a', 'b', 'c', getdate(), 1, '7:d41d8cd98f00b204e9800998ecf8427e', 'empty', '', 'executed', '"+version+"')", MSSQLDatabase.class);
        assertCorrect("insert into databasechangelog (id, author, filename, dateexecuted, orderexecuted, md5sum, description, comments, exectype, liquibase) values ('a', 'b', 'c', systimestamp, 1, '7:d41d8cd98f00b204e9800998ecf8427e', 'empty', '', 'executed', '"+version+"')",OracleDatabase.class);
        assertCorrect("insert into [databasechangelog] ([id], [author], [filename], [dateexecuted], [orderexecuted], [md5sum], [description], [comments], [exectype], [liquibase]) values ('a', 'b', 'c', getdate(), 1, '7:d41d8cd98f00b204e9800998ecf8427e', 'empty', '', 'executed', '"+version+"')",SybaseDatabase.class);
        assertCorrect("insert into databasechangelog (id, author, filename, dateexecuted, orderexecuted, md5sum, description, comments, exectype, liquibase) values ('a', 'b', 'c', current year to fraction(5), 1, '7:d41d8cd98f00b204e9800998ecf8427e', 'empty', '', 'executed', '"+version+"')",InformixDatabase.class);
        assertCorrect("insert into databasechangelog (id, author, filename, dateexecuted, orderexecuted, md5sum, description, comments, exectype, liquibase) values ('a', 'b', 'c', current timestamp, 1, '7:d41d8cd98f00b204e9800998ecf8427e', 'empty', '', 'executed', '"+version+"')",DB2Database.class);
        assertCorrect("insert into databasechangelog (id, author, filename, dateexecuted, orderexecuted, md5sum, description, comments, exectype, liquibase) values ('a', 'b', 'c', current_timestamp, 1, '7:d41d8cd98f00b204e9800998ecf8427e', 'empty', '', 'executed', '"+version+"')",FirebirdDatabase.class, DerbyDatabase.class);
        assertCorrect("insert into databasechangelog (id, author, filename, dateexecuted, orderexecuted, md5sum, description, comments, exectype, liquibase) values ('a', 'b', 'c', now, 1, '7:d41d8cd98f00b204e9800998ecf8427e', 'empty', '', 'executed', '"+version+"')",HsqlDatabase.class);
        assertCorrect("insert into databasechangelog (id, author, filename, dateexecuted, orderexecuted, md5sum, description, comments, exectype, liquibase) values ('a', 'b', 'c', now(), 1, '7:d41d8cd98f00b204e9800998ecf8427e', 'empty', '', 'executed', '"+version+"')",SybaseASADatabase.class);
        assertCorrect("insert into databasechangelog (id, author, filename, dateexecuted, orderexecuted, md5sum, description, comments, exectype, liquibase) values ('a', 'b', 'c', now(), 1, '7:d41d8cd98f00b204e9800998ecf8427e', 'empty', '', 'executed', '"+version+"')",MySQLDatabase.class,PostgresDatabase.class,H2Database.class);
        assertCorrectOnRest("insert into databasechangelog (id, author, filename, dateexecuted, orderexecuted, md5sum, description, comments, exectype, liquibase) values ('a', 'b', 'c', current timestamp, 1, '7:d41d8cd98f00b204e9800998ecf8427e', 'empty', '', 'executed', '"+version+"')");
    }

    @Test
    public void generateSql_update() throws Exception {
        this.statementUnderTest = new MarkChangeSetRanStatement(new ChangeSet("a", "b", false, false, "c", "e", "f", null), ChangeSet.ExecType.RERAN);
        assertCorrect("update [databasechangelog] set [dateexecuted] = NOW(), [exectype] = 'reran', [md5sum] = '7:d41d8cd98f00b204e9800998ecf8427e' where id='a' and author='b' and filename='c'", MSSQLDatabase.class);
        assertCorrect("update [databasechangelog] set [dateexecuted] = SYSTIMESTAMP, [exectype] = 'reran', [md5sum] = '7:d41d8cd98f00b204e9800998ecf8427e' where id='a' and author='b' and filename='c'", OracleDatabase.class);
        assertCorrect("update [databasechangelog] set [dateexecuted] = getdate(), [exectype] = 'reran', [md5sum] = '7:d41d8cd98f00b204e9800998ecf8427e' where id='a' and author='b' and filename='c'", SybaseDatabase.class);
        assertCorrect("update [databasechangelog] set [dateexecuted] = current year to fraction(5), [exectype] = 'reran', [md5sum] = '7:d41d8cd98f00b204e9800998ecf8427e' where id='a' and author='b' and filename='c'", InformixDatabase.class);
        assertCorrect("update [databasechangelog] set [dateexecuted] = current timestamp, [exectype] = 'reran', [md5sum] = '7:d41d8cd98f00b204e9800998ecf8427e' where id='a' and author='b' and filename='c'", DB2Database.class);
        assertCorrect("update [databasechangelog] set [dateexecuted] = current_timestamp, [exectype] = 'reran', [md5sum] = '7:d41d8cd98f00b204e9800998ecf8427e' where id='a' and author='b' and filename='c'", FirebirdDatabase.class, DerbyDatabase.class);
        assertCorrect("update [databasechangelog] set [dateexecuted] = NOW(), [exectype] = 'reran', [md5sum] = '7:d41d8cd98f00b204e9800998ecf8427e' where id='a' and author='b' and filename='c'", SybaseASADatabase.class);
        assertCorrect("update [databasechangelog] set [dateexecuted] = NOW(), [exectype] = 'reran', [md5sum] = '7:d41d8cd98f00b204e9800998ecf8427e' where id='a' and author='b' and filename='c'", MySQLDatabase.class,HsqlDatabase.class,PostgresDatabase.class,H2Database.class);
        assertCorrectOnRest("update [databasechangelog] set [dateexecuted] = NOW(), [exectype] = 'reran', [md5sum] = '7:d41d8cd98f00b204e9800998ecf8427e' where id='a' and author='b' and filename='c'");
    }
}