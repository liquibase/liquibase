package liquibase.statementexecute;

import liquibase.Scope;
import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.CreateDatabaseChangeLogTableStatement;
import liquibase.statement.core.MarkChangeSetRanStatement;
import liquibase.util.LiquibaseUtil;
import liquibase.util.StringUtil;
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
        Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class).resetAll();

        this.statementUnderTest = new MarkChangeSetRanStatement(new ChangeSet("a", "b", false, false, "c", "e", "f",
                null), ChangeSet.ExecType.EXECUTED);
        String deploymentId = Scope.getCurrentScope().getDeploymentId();
        String version = StringUtil.limitSize(LiquibaseUtil.getBuildVersion()
                .replaceAll("SNAPSHOT", "SNP")
                .replaceAll("beta", "b")
                .replaceAll("alpha", "a")
                , 20);
        assertCorrect(String.format("insert into [databasechangelog] ([id], [author], [filename], [dateexecuted], " +
                        "[orderexecuted], [md5sum], [description], [comments], [exectype], [contexts], [labels], " +
                        "[liquibase], [deployment_id]) values ('a', 'b', 'c', getdate(), 1, " +
                        "'9:d41d8cd98f00b204e9800998ecf8427e', 'empty', '', 'executed', 'e', null, '" + version + "'," +
                        " '%s')", deploymentId),
                MSSQLDatabase.class);
        assertCorrect(String.format("insert into databasechangelog (id, author, filename, dateexecuted, orderexecuted, " +
                        "md5sum, description, comments, exectype, contexts, labels, liquibase, deployment_id) values " +
                        "('a', 'b', 'c', systimestamp, 1, '9:d41d8cd98f00b204e9800998ecf8427e', 'empty', '', " +
                        "'executed', 'e', null, '" + version + "', '%s')", deploymentId),
                OracleDatabase.class);
        assertCorrect(String.format("insert into [databasechangelog] ([id], [author], [filename], [dateexecuted], " +
                        "[orderexecuted], [md5sum], [description], [comments], [exectype], [contexts], [labels], " +
                        "[liquibase], [deployment_id]) values ('a', 'b', 'c', getdate(), 1, " +
                        "'9:d41d8cd98f00b204e9800998ecf8427e', 'empty', '', 'executed', 'e', null, '" + version + "'," +
                        " '%s')", deploymentId),
                SybaseDatabase.class);
        assertCorrect(String.format("insert into databasechangelog (id, author, filename, dateexecuted, orderexecuted, " +
                        "md5sum, description, comments, exectype, contexts, labels, liquibase, deployment_id) values " +
                        "('a', 'b', 'c', " +
                        "current year to fraction(5), 1, '9:d41d8cd98f00b204e9800998ecf8427e', 'empty', '', " +
                        "'executed', " +
                        "'e', null, '" + version + "', '%s')", deploymentId),
                InformixDatabase.class);
        assertCorrect(String.format("insert into databasechangelog (id, author, filename, dateexecuted, orderexecuted, " +
                        "md5sum, description, comments, exectype, contexts, labels, liquibase, deployment_id) values " +
                        "('a', 'b', 'c', current timestamp, 1, " +
                        "'9:d41d8cd98f00b204e9800998ecf8427e', 'empty', '', 'executed', 'e', null, '" + version + "'," +
                        " '%s')", deploymentId),
                DB2Database.class);
        assertCorrect(String.format("insert into databasechangelog (id, author, filename, dateexecuted, orderexecuted, " +
                        "md5sum, description, comments, exectype, contexts, labels, liquibase, deployment_id) values " +
                        "('a', 'b', 'c', current_timestamp, 1, " +
                        "'9:d41d8cd98f00b204e9800998ecf8427e', 'empty', '', 'executed', 'e', null, '" + version + "'," +
                        " '%s')", deploymentId),
                FirebirdDatabase.class, DerbyDatabase.class);
        assertCorrect(String.format("insert into databasechangelog (id, author, filename, dateexecuted, orderexecuted, " +
                        "md5sum, description, comments, exectype, contexts, labels, liquibase, deployment_id) values " +
                        "('a', 'b', 'c', now, 1, " +
                        "'9:d41d8cd98f00b204e9800998ecf8427e', 'empty', '', 'executed', 'e', null, '" + version + "'," +
                        " '%s')", deploymentId),
                HsqlDatabase.class);
        assertCorrect(String.format("insert into databasechangelog (id, author, filename, dateexecuted, orderexecuted, " +
                        "md5sum, description, comments, exectype, contexts, labels, liquibase, deployment_id) values " +
                        "('a', 'b', 'c', now(), 1, " +
                        "'9:d41d8cd98f00b204e9800998ecf8427e', 'empty', '', 'executed', 'e', null, '" + version + "'," +
                        " '%s')", deploymentId),
                SybaseASADatabase.class);
        assertCorrect(String.format("insert into databasechangelog (id, author, filename, dateexecuted, orderexecuted, " +
                        "md5sum, `description`, comments, exectype, contexts, labels, liquibase, deployment_id) values " +
                        "('a', 'b', 'c', now(), 1, " +
                        "'9:d41d8cd98f00b204e9800998ecf8427e', 'empty', '', 'executed', 'e', null, '" + version + "'," +
                        " '%s')", deploymentId),
                MySQLDatabase.class);
        assertCorrect(String.format("insert into databasechangelog (id, author, filename, dateexecuted, orderexecuted, " +
                        "md5sum, `description`, comments, exectype, contexts, labels, liquibase, deployment_id) values " +
                        "('a', 'b', 'c', now(), 1, " +
                        "'9:d41d8cd98f00b204e9800998ecf8427e', 'empty', '', 'executed', 'e', null, '" + version + "'," +
                        " '%s')", deploymentId),
                MariaDBDatabase.class);
        assertCorrect(String.format("insert into databasechangelog (id, author, filename, dateexecuted, orderexecuted, " +
                        "md5sum, description, comments, exectype, contexts, labels, liquibase, deployment_id) values " +
                        "('a', 'b', 'c', now(), 1, " +
                        "'9:d41d8cd98f00b204e9800998ecf8427e', 'empty', '', 'executed', 'e', null, '" + version + "'," +
                        " '%s')", deploymentId),
                PostgresDatabase.class, H2Database.class, CockroachDatabase.class, EnterpriseDBDatabase.class);
        assertCorrect(String.format("insert into databasechangelog (id, author, filename, dateexecuted, orderexecuted, " +
                        "md5sum, description, comments, exectype, contexts, labels, liquibase, deployment_id) values " +
                        "('a', 'b', 'c', date('now'), 1, " +
                        "'9:d41d8cd98f00b204e9800998ecf8427e', 'empty', '', 'executed', 'e', null, '" + version + "'," +
                        " '%s')", deploymentId),
                Ingres9Database.class);
        assertCorrect(String.format("insert into databasechangelog (id, author, filename, dateexecuted, orderexecuted, " +
                        "md5sum, description, comments, exectype, contexts, labels, liquibase, deployment_id) values " +
                        "('a', 'b', 'c', current_timestamp::timestamp_ntz, 1," +
                        " '9:d41d8cd98f00b204e9800998ecf8427e', 'empty', '', 'executed', 'e', null, " +
                        "'" + version + "', '%s')", deploymentId),
                SnowflakeDatabase.class);
        assertCorrectOnRest(String.format("insert into databasechangelog (id, author, filename, dateexecuted, " +
                "orderexecuted, md5sum, description, comments, exectype, contexts, labels, liquibase, deployment_id) " +
                "values ('a', 'b', 'c', " +
                "current timestamp, 1, '9:d41d8cd98f00b204e9800998ecf8427e', 'empty', '', 'executed', 'e', null, " +
                "'" + version + "', '%s')", deploymentId));
    }

    @Test
    public void generateSql_update() throws Exception {
        Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class).resetAll();

        this.statementUnderTest = new MarkChangeSetRanStatement(new ChangeSet("a", "b", false, false, "c", "e", "f",
                null), ChangeSet.ExecType.RERAN);

        String deploymentId = Scope.getCurrentScope().getDeploymentId();

        assertCorrect(String.format("update [databasechangelog] set [comments] = '', [contexts] = 'e', [dateexecuted] = getdate(), [deployment_id] = '%s', [description] = 'empty', [exectype] " +
                        "= 'reran', [labels] = null, liquibase = 'dev', [md5sum] = '9:d41d8cd98f00b204e9800998ecf8427e', [orderexecuted] = 1 where [id] =" +
                        " 'a' and" +
                        " [author] = 'b' and [filename] = 'c'", deploymentId),
                MSSQLDatabase.class);
        assertCorrect(String.format("update databasechangelog set comments = '', contexts = 'e', dateexecuted = systimestamp, deployment_id = '%s', [description] = 'empty', exectype = " +
                        "'reran', labels = null, liquibase = 'dev', md5sum = '9:d41d8cd98f00b204e9800998ecf8427e', orderexecuted = 1 where id = 'a' and" +
                        " author " +
                        "= 'b' and filename = 'c'", deploymentId),
                OracleDatabase.class);
        assertCorrect(String.format("update [databasechangelog] set [comments] = '', [contexts] = 'e', [dateexecuted] = getdate(), [deployment_id] = '%s', [description] = 'empty', [exectype] " +
                "= 'reran', [labels] = null, liquibase = 'dev', [md5sum] = '9:d41d8cd98f00b204e9800998ecf8427e', [orderexecuted] = 1 where [id] = 'a' and" +
                " [author] = 'b' and [filename] = 'c'", deploymentId), SybaseDatabase.class);
        assertCorrect(String.format("update [databasechangelog] set [comments] = '', [contexts] = 'e', [dateexecuted] = current year to fraction(5), deployment_id = '%s', [description] = 'empty', exectype = 'reran', [labels] = null, liquibase = 'dev', md5sum = '9:d41d8cd98f00b204e9800998ecf8427e', orderexecuted = 1 where id " +
                "= 'a' and author = 'b' and filename = 'c'", deploymentId), InformixDatabase.class);
        assertCorrect(String.format("update [databasechangelog] set [comments] = '', [contexts] = 'e', [dateexecuted] = current timestamp, deployment_id = '%s', [description] = 'empty', " +
                        "exectype = 'reran', [labels] = null, liquibase = 'dev', md5sum = '9:d41d8cd98f00b204e9800998ecf8427e', orderexecuted = 1 where " +
                        "id = 'a' and author = 'b' and filename = 'c'", deploymentId),
                DB2Database.class);
        assertCorrect(String.format("update [databasechangelog] set [comments] = '', [contexts] = 'e', [dateexecuted] = current_timestamp, deployment_id = '%s', [description] = 'empty', " +
                        "exectype = 'reran', [labels] = null, liquibase = 'dev', md5sum = '9:d41d8cd98f00b204e9800998ecf8427e', orderexecuted = 1 where " +
                        "id = 'a' and author = 'b' and filename = 'c'", deploymentId),
                FirebirdDatabase.class,
                DerbyDatabase.class);
        assertCorrect(String.format("update [databasechangelog] set [comments] = '', [contexts] = 'e', [dateexecuted] = NOW(), deployment_id = '%s', [description] = 'empty', exectype = " +
                        "'reran', [labels] = null, liquibase = 'dev', md5sum = '9:d41d8cd98f00b204e9800998ecf8427e', orderexecuted = 1 where id = 'a' and" +
                        " author = 'b' and filename = 'c'", deploymentId),
                SybaseASADatabase.class);
        assertCorrect(String.format("update [databasechangelog] set [comments] = '', [contexts] = 'e', [dateexecuted] = NOW(), deployment_id = '%s', [description] = 'empty', exectype = " +
                        "'reran', [labels] = null, liquibase = 'dev', md5sum = '9:d41d8cd98f00b204e9800998ecf8427e', orderexecuted = 1 where id = 'a' and" +
                        " author = 'b' and filename = 'c'", deploymentId),
                MySQLDatabase.class, MariaDBDatabase.class, HsqlDatabase.class, PostgresDatabase.class, H2Database.class, CockroachDatabase.class);
        assertCorrectOnRest(String.format("update [databasechangelog] set [comments] = '', [contexts] = 'e', [dateexecuted] = NOW(), [deployment_id] = '%s', [description] = 'empty', [exectype] = 'reran', [labels] = null, liquibase = 'dev', [md5sum] = " +
                "'9:d41d8cd98f00b204e9800998ecf8427e', [orderexecuted] = 1 where id = 'a' and author = 'b' and filename = 'c'", deploymentId));
    }
}
