package liquibase.statementexecute;

import liquibase.database.Database;
import liquibase.database.InformixDatabase;
import liquibase.database.HsqlDatabase;
import liquibase.database.SybaseASADatabase;
import liquibase.test.TestContext;
import liquibase.statement.*;

import java.util.List;
import java.util.ArrayList;

import org.junit.Test;

public class AddColumnExecutorTest extends AbstractExecuteTest {

    protected static final String TABLE_NAME = "table_name";
    
    @Override
    protected List<? extends SqlStatement> setupStatements(Database database) {
        ArrayList<CreateTableStatement> statements = new ArrayList<CreateTableStatement>();
        CreateTableStatement table = new CreateTableStatement(null, TABLE_NAME);
        table.addColumn("id", "int", new NotNullConstraint());
        statements.add(table);

        if (database.supportsSchemas()) {
            table = new CreateTableStatement(TestContext.ALT_SCHEMA, TABLE_NAME);
            table.addColumn("id", "int", new NotNullConstraint());
            statements.add(table);
        }
        return statements;
    }

    @Test
    public void generateSql_autoIncrement() throws Exception {
        this.statementUnderTest = new AddColumnStatement(null, "table_name", "column_name", "int", null, new AutoIncrementConstraint("column_name"));

        assertCorrect("alter table table_name add column_name serial", InformixDatabase.class);
        assertCorrect("alter table [table_name] add [column_name] autoincrement", SybaseASADatabase.class);
        assertCorrect("alter table table_name add column_name int auto_increment_clause");
    }

    @Test
    public void generateSql_notNull() throws Exception {
        this.statementUnderTest = new AddColumnStatement(null, "table_name", "column_name", "int", 42, new NotNullConstraint());
        assertCorrect("ALTER TABLE [table_name] ADD [column_name] int DEFAULT 42 NOT NULL");
    }

    @SuppressWarnings("unchecked")
	@Test
    public void generateSql_primaryKey() throws Exception {
        this.statementUnderTest = new AddColumnStatement(null, "table_name", "column_name", "int", null, new PrimaryKeyConstraint());

        assertCorrect("ALTER TABLE [table_name] ADD [column_name] INT NOT NULL PRIMARY KEY", HsqlDatabase.class);
        assertCorrect("ALTER TABLE [table_name] ADD [column_name] int PRIMARY KEY NOT NULL");
    }

}
