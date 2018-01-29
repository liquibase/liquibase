package liquibase.statementexecute;

import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.CreateDatabaseChangeLogTableStatement;
import liquibase.statement.core.MarkChangeSetRanStatement;
import liquibase.util.LiquibaseUtil;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class MarkChangeSetRanExecuteTest extends AbstractExecuteTest {
    @Override
    protected List<? extends SqlStatement> setupStatements(Database database) {
        return Arrays.asList(new CreateDatabaseChangeLogTableStatement());
    }

    @Test
    public void generateSql_insert() throws Exception {
        this.statementUnderTest = new MarkChangeSetRanStatement(new ChangeSet("a", "b", false, false, "c", "e", "f",
                null), ChangeSet.ExecType.EXECUTED);
        String version = LiquibaseUtil.getBuildVersion().replaceAll("SNAPSHOT", "SNP");
        assertCorrect("insert into [databasechangelog] ([id], [author], [filename], [dateexecuted], " +
                        "[orderexecuted], [md5sum], [description], [comments], [exectype], [contexts], [labels], " +
                        "[liquibase], [deployment_id]) values ('a', 'b', 'c', getdate(), 1, " +
                        "'8:d41d8cd98f00b204e9800998ecf8427e', 'empty', '', 'executed', 'e', null, '" + version + "'," +
                        " null)",
                MSSQLDatabase.class);
        assertCorrect("insert into databasechangelog (id, author, filename, dateexecuted, orderexecuted, " +
                        "md5sum, description, comments, exectype, contexts, labels, liquibase, deployment_id) values " +
                        "('a', 'b', 'c', systimestamp, 1, '8:d41d8cd98f00b204e9800998ecf8427e', 'empty', '', " +
                        "'executed', 'e', null, '" + version + "', null)",
                OracleDatabase.class);
        assertCorrect("insert into [databasechangelog] ([id], [author], [filename], [dateexecuted], " +
                        "[orderexecuted], [md5sum], [description], [comments], [exectype], [contexts], [labels], " +
                        "[liquibase], [deployment_id]) values ('a', 'b', 'c', getdate(), 1, " +
                        "'8:d41d8cd98f00b204e9800998ecf8427e', 'empty', '', 'executed', 'e', null, '" + version + "'," +
                        " null)",
                SybaseDatabase.class);
        assertCorrect("insert into databasechangelog (id, author, filename, dateexecuted, orderexecuted, " +
                        "md5sum, description, comments, exectype, contexts, labels, liquibase, deployment_id) values " +
                        "('a', 'b', 'c', " +
                        "current year to fraction(5), 1, '8:d41d8cd98f00b204e9800998ecf8427e', 'empty', '', " +
                        "'executed', " +
                        "'e', null, '" + version + "', null)",
                InformixDatabase.class);
        assertCorrect("insert into databasechangelog (id, author, filename, dateexecuted, orderexecuted, " +
                        "md5sum, description, comments, exectype, contexts, labels, liquibase, deployment_id) values " +
                        "('a', 'b', 'c', current timestamp, 1, " +
                        "'8:d41d8cd98f00b204e9800998ecf8427e', 'empty', '', 'executed', 'e', null, '" + version + "'," +
                        " null)",
                DB2Database.class);
        assertCorrect("insert into databasechangelog (id, author, filename, dateexecuted, orderexecuted, " +
                        "md5sum, description, comments, exectype, contexts, labels, liquibase, deployment_id) values " +
                        "('a', 'b', 'c', current_timestamp, 1, " +
                        "'8:d41d8cd98f00b204e9800998ecf8427e', 'empty', '', 'executed', 'e', null, '" + version + "'," +
                        " null)",
                FirebirdDatabase.class, DerbyDatabase.class);
        assertCorrect("insert into databasechangelog (id, author, filename, dateexecuted, orderexecuted, " +
                        "md5sum, description, comments, exectype, contexts, labels, liquibase, deployment_id) values " +
                        "('a', 'b', 'c', now, 1, " +
                        "'8:d41d8cd98f00b204e9800998ecf8427e', 'empty', '', 'executed', 'e', null, '" + version + "'," +
                        " null)",
                HsqlDatabase.class);
        assertCorrect("insert into databasechangelog (id, author, filename, dateexecuted, orderexecuted, " +
                        "md5sum, description, comments, exectype, contexts, labels, liquibase, deployment_id) values " +
                        "('a', 'b', 'c', now(), 1, " +
                        "'8:d41d8cd98f00b204e9800998ecf8427e', 'empty', '', 'executed', 'e', null, '" + version + "'," +
                        " null)",
                SybaseASADatabase.class);
        assertCorrect("insert into databasechangelog (id, author, filename, dateexecuted, orderexecuted, " +
                        "md5sum, description, comments, exectype, contexts, labels, liquibase, deployment_id) values " +
                        "('a', 'b', 'c', now(), 1, " +
                        "'8:d41d8cd98f00b204e9800998ecf8427e', 'empty', '', 'executed', 'e', null, '" + version + "'," +
                        " null)",
                MySQLDatabase.class, MariaDBDatabase.class, PostgresDatabase.class, H2Database.class);
        assertCorrectOnRest("insert into databasechangelog (id, author, filename, dateexecuted, " +
                "orderexecuted, md5sum, description, comments, exectype, contexts, labels, liquibase, deployment_id) " +
                "values ('a', 'b', 'c', " +
                "current timestamp, 1, '8:d41d8cd98f00b204e9800998ecf8427e', 'empty', '', 'executed', 'e', null, " +
                "'" + version + "', null)");
    }

    @Test
    public void generateSql_update() throws Exception {
        this.statementUnderTest = new MarkChangeSetRanStatement(new ChangeSet("a", "b", false, false, "c", "e", "f",
                null), ChangeSet.ExecType.RERAN);
        assertCorrect("update [databasechangelog] set [dateexecuted] = getdate(), [deployment_id] = null, [exectype] " +
                        "= 'reran', [md5sum] = '8:d41d8cd98f00b204e9800998ecf8427e', [orderexecuted] = 2 where [id] =" +
                        " 'a' and" +
                        " [author] = 'b' and [filename] = 'c'",
                MSSQLDatabase.class);
        assertCorrect("update databasechangelog set dateexecuted = systimestamp, deployment_id = null, exectype = " +
                        "'reran', md5sum = '8:d41d8cd98f00b204e9800998ecf8427e', orderexecuted = 2 where id = 'a' and" +
                        " author " +
                        "= 'b' and filename = 'c'",
                OracleDatabase.class);
        assertCorrect("update [databasechangelog] set [dateexecuted] = getdate(), [deployment_id] = null, [exectype] " +
                "= 'reran', [md5sum] = '8:d41d8cd98f00b204e9800998ecf8427e', [orderexecuted] = 2 where [id] = 'a' and" +
                " [author] = 'b' and [filename] = 'c'", SybaseDatabase.class);
        assertCorrect("update [databasechangelog] set [dateexecuted] = current year to fraction(5), deployment_id = " +
                "null, exectype = 'reran', md5sum = '8:d41d8cd98f00b204e9800998ecf8427e', orderexecuted = 2 where id " +
                "= 'a' and author = 'b' and filename = 'c'", InformixDatabase.class);
        assertCorrect("update [databasechangelog] set [dateexecuted] = current timestamp, deployment_id = null, " +
                        "exectype = 'reran', md5sum = '8:d41d8cd98f00b204e9800998ecf8427e', orderexecuted = 2 where " +
                        "id = 'a' and author = 'b' and filename = 'c'",
                DB2Database.class);
        assertCorrect("update [databasechangelog] set [dateexecuted] = current_timestamp, deployment_id = null, " +
                        "exectype = 'reran', md5sum = '8:d41d8cd98f00b204e9800998ecf8427e', orderexecuted = 2 where " +
                        "id = 'a' and author = 'b' and filename = 'c'",
                FirebirdDatabase.class,
                DerbyDatabase.class);
        assertCorrect("update [databasechangelog] set [dateexecuted] = NOW(), deployment_id = null, exectype = " +
                        "'reran', md5sum = '8:d41d8cd98f00b204e9800998ecf8427e', orderexecuted = 2 where id = 'a' and" +
                        " author = 'b' and filename = 'c'",
                SybaseASADatabase.class);
        assertCorrect("update [databasechangelog] set [dateexecuted] = NOW(), deployment_id = null, exectype = " +
                        "'reran', md5sum = '8:d41d8cd98f00b204e9800998ecf8427e', orderexecuted = 2 where id = 'a' and" +
                        " author = 'b' and filename = 'c'",
                MySQLDatabase.class, MariaDBDatabase.class, HsqlDatabase.class, PostgresDatabase.class, H2Database
                        .class);
        assertCorrectOnRest("update [databasechangelog] set [dateexecuted] = NOW(), [deployment_id] = null, [exectype] = 'reran', [md5sum] = " +
                "'8:d41d8cd98f00b204e9800998ecf8427e', [orderexecuted] = 2 where id = 'a' and author = 'b' and filename = 'c'");
    }
}