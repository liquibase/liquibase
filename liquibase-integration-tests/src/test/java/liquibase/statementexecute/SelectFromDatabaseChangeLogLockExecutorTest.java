package liquibase.statementexecute;

import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.CreateDatabaseChangeLogLockTableStatement;
import liquibase.statement.core.SelectFromDatabaseChangeLogLockStatement;

import java.util.List;
import java.util.Arrays;

import org.junit.Test;

public class SelectFromDatabaseChangeLogLockExecutorTest extends AbstractExecuteTest {

    @Override
    protected List<? extends SqlStatement> setupStatements(Database database) {
        return Arrays.asList(new CreateDatabaseChangeLogLockTableStatement());
    }

    @Test
    public void generateSql() throws Exception {
        this.statementUnderTest = new SelectFromDatabaseChangeLogLockStatement("LOCKED");
        assertCorrect("select [locked] from [dbo].[databasechangeloglock] where [id]=1", MSSQLDatabase.class);
        assertCorrect("select [locked] from [databasechangeloglock] where [id]=1 for update", OracleDatabase.class);
        assertCorrectOnRest("select [locked] from [databasechangeloglock] where [id]=1");
    }

    @Test
    public void generateSql_count() throws Exception {
        this.statementUnderTest = new SelectFromDatabaseChangeLogLockStatement("COUNT(*)");
        assertCorrect("select count(*) from [dbo].[databasechangeloglock] where [id]=1", MSSQLDatabase.class);
        assertCorrect("select count(*) from [databasechangeloglock] where [id]=1 for update", OracleDatabase.class);
        assertCorrectOnRest("select count(*) from [databasechangeloglock] where [id]=1");
    }

    @Test
    public void generateSql_multicolumn() throws Exception {
        this.statementUnderTest = new SelectFromDatabaseChangeLogLockStatement("LOCKED", "LOCKEDBY");
        assertCorrect("select [locked],[lockedby] from [dbo].[databasechangeloglock] where [id]=1", MSSQLDatabase.class);
        assertCorrect("select [locked],[lockedby] from [databasechangeloglock] where [id]=1 for update", OracleDatabase.class);
        assertCorrectOnRest("select [locked],[lockedby] from [databasechangeloglock] where [id]=1");
    }

}
