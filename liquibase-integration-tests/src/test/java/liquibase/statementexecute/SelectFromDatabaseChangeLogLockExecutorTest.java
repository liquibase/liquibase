package liquibase.statementexecute;

import liquibase.database.Database;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.CreateDatabaseChangeLogLockTableStatement;

import java.util.Arrays;
import java.util.List;

public class SelectFromDatabaseChangeLogLockExecutorTest extends AbstractExecuteTest {

    @Override
    protected List<? extends SqlStatement> setupStatements(Database database) {
        return Arrays.asList(new CreateDatabaseChangeLogLockTableStatement());
    }

//    @Test
//    public void generateSql() throws Exception {
//        this.statementUnderTest = new SelectFromDatabaseChangeLogLockStatement("LOCKED");
//        assertCorrect("select [locked] from [databasechangeloglock] where [id]=1", MSSQLDatabase.class, SybaseDatabase.class);
//        assertCorrect("select [locked] from [databasechangeloglock] where [id]=1", SybaseASADatabase.class);
//        assertCorrect("select [locked] from [databasechangeloglock] where [id]=1 for update", OracleDatabase.class);
//        assertCorrectOnRest("select [locked] from [databasechangeloglock] where [id]=1");
//    }
//
//    @Test
//    public void generateSql_count() throws Exception {
//        this.statementUnderTest = new SelectFromDatabaseChangeLogLockStatement(new ColumnConfig().setName("COUNT(*)", true));
//        assertCorrect("select count(*) from [databasechangeloglock] where [id]=1", MSSQLDatabase.class, SybaseDatabase.class);
//        assertCorrect("select count(*) from [databasechangeloglock] where [id]=1", MSSQLDatabase.class, SybaseASADatabase.class);
//        assertCorrect("select count(*) from [databasechangeloglock] where [id]=1 for update", OracleDatabase.class);
//        assertCorrectOnRest("select count(*) from [databasechangeloglock] where [id]=1");
//    }
//
//    @Test
//    public void generateSql_multicolumn() throws Exception {
//        this.statementUnderTest = new SelectFromDatabaseChangeLogLockStatement("LOCKED", "LOCKEDBY");
//        assertCorrect("select [locked],[lockedby] from [databasechangeloglock] where [id]=1", MSSQLDatabase.class, SybaseDatabase.class);
//        assertCorrect("select [locked],[lockedby] from [databasechangeloglock] where [id]=1", MSSQLDatabase.class, SybaseASADatabase.class);
//        assertCorrect("select [locked],[lockedby] from [databasechangeloglock] where [id]=1 for update", OracleDatabase.class);
//        assertCorrectOnRest("select [locked],[lockedby] from [databasechangeloglock] where [id]=1");
//    }

}
