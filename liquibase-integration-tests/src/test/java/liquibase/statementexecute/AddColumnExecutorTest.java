package liquibase.statementexecute;

import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.MaxDBDatabase;
import liquibase.database.core.MySQLDatabase;
import liquibase.database.core.PostgresDatabase;
import liquibase.database.core.SQLiteDatabase;
import liquibase.database.core.SybaseASADatabase;
import liquibase.database.core.SybaseDatabase;
import liquibase.database.core.CacheDatabase;
import liquibase.database.core.*;
import liquibase.test.TestContext;
import liquibase.test.DatabaseTestContext;
import liquibase.statement.*;
import liquibase.statement.core.AddColumnStatement;
import liquibase.statement.core.CreateTableStatement;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.sql.Sql;

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
            table = new CreateTableStatement(DatabaseTestContext.ALT_SCHEMA, TABLE_NAME);
            table.addColumn("id", "int", new NotNullConstraint());
            statements.add(table);
        }
        return statements;
    }

    @SuppressWarnings("unchecked")
	@Test
    public void generateSql_autoIncrement() throws Exception {
        this.statementUnderTest = new AddColumnStatement(null, "table_name", "column_name", "int", null, new AutoIncrementConstraint("column_name"));

        assertCorrect("alter table table_name add column_name serial", InformixDatabase.class);
        assertCorrect("alter table [table_name] add [column_name] int default autoincrement null", SybaseASADatabase.class);
        assertCorrect("alter table [table_name] add [column_name] serial", PostgresDatabase.class);
        assertCorrect("alter table [dbo].[table_name] add [column_name] int identity", MSSQLDatabase.class);
        assertCorrect("alter table [table_name] add [column_name] int identity null", SybaseDatabase.class);
        assertCorrect("not supported. fixme!!", SQLiteDatabase.class);
        assertCorrect("alter table table_name add column_name int auto_increment_clause");
    }

    @SuppressWarnings("unchecked")
	@Test
    public void generateSql_notNull() throws Exception {
        this.statementUnderTest = new AddColumnStatement(null, "table_name", "column_name", "int", 42, new NotNullConstraint());
        assertCorrect("alter table [table_name] add [column_name] int not null default 42", SybaseASADatabase.class, SybaseDatabase.class, CacheDatabase.class, MaxDBDatabase.class);
        assertCorrect("alter table table_name add column_name int not null default 42", PostgresDatabase.class);
        assertCorrect("alter table [dbo].[table_name] add [column_name] int not null constraint df_table_name_column_name default 42", MSSQLDatabase.class);
        assertCorrect("alter table `table_name` add `column_name` int not null default 42", MySQLDatabase.class);
        assertCorrect("not supported. fixme!!", SQLiteDatabase.class);
        assertCorrect("ALTER TABLE [table_name] ADD [column_name] int DEFAULT 42 NOT NULL");
    }

    @SuppressWarnings("unchecked")
	@Test
    public void generateSql_primaryKey() throws Exception {
        this.statementUnderTest = new AddColumnStatement(null, "table_name", "column_name", "int", null, new PrimaryKeyConstraint());

        assertCorrect("alter table [table_name] add [column_name] int not null primary key", HsqlDatabase.class, SybaseASADatabase.class, SybaseDatabase.class, MaxDBDatabase.class);
        assertCorrect("alter table [dbo].[table_name] add [column_name] int not null primary key", MSSQLDatabase.class);
        assertCorrect("alter table table_name add column_name int not null primary key", PostgresDatabase.class);
        assertCorrect("alter table `table_name` add `column_name` int not null primary key", MySQLDatabase.class);
        assertCorrect("ALTER TABLE [table_name] ADD [column_name] int PRIMARY KEY NOT NULL");
    }

    @SuppressWarnings("unchecked")
	@Test
    public void generateSql_foreignKey() throws Exception {
        this.statementUnderTest = new AddColumnStatement(null, "table_name", "column_name", "int", null, new PrimaryKeyConstraint(), new ForeignKeyConstraint("fk_test_fk", "table_name(column_name)"));

        assertCorrect(new String[] {"alter table [table_name] add [column_name] int not null primary key", "alter table [table_name] add constraint [fk_test_fk] foreign key ([column_name]) references [table_name]([column_name])"}, HsqlDatabase.class, SybaseASADatabase.class, SybaseDatabase.class, MaxDBDatabase.class);
        assertCorrect(new String[] {"alter table [dbo].[table_name] add [column_name] int not null primary key", "alter table [dbo].[table_name] add constraint [fk_test_fk] foreign key ([column_name]) references [dbo].[table_name]([column_name])"}, MSSQLDatabase.class);
        assertCorrect(new String[] {"alter table table_name add column_name int not null primary key", "alter table [table_name] add constraint [fk_test_fk] foreign key ([column_name]) references [table_name]([column_name])"}, PostgresDatabase.class);
        assertCorrect(new String[] {"alter table `table_name` add `column_name` int not null primary key", "alter table [table_name] add constraint [fk_test_fk] foreign key ([column_name]) references [table_name]([column_name])"}, MySQLDatabase.class);
        assertCorrect(new String[] {"ALTER TABLE [table_name] ADD [column_name] int PRIMARY KEY NOT NULL", "alter table [table_name] add constraint  foreign key ([column_name]) references [table_name]([column_name]) constraint [fk_test_fk]"}, InformixDatabase.class);
        assertCorrect(new String[] {"ALTER TABLE [table_name] ADD [column_name] int PRIMARY KEY NOT NULL", "alter table [table_name] add constraint [fk_test_fk] foreign key ([column_name]) references [table_name]([column_name])"});
    }

}
