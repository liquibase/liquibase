package liquibase.statement.generator;

import liquibase.database.*;
import liquibase.statement.*;
import liquibase.test.TestContext;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class AddColumnGeneratorTest extends AbstractSqlGeneratorTest<AddColumnStatement> {

	protected static final String TABLE_NAME = "table_name";

    public AddColumnGeneratorTest() throws Exception {
        this(new AddColumnGenerator());
    }

    public AddColumnGeneratorTest(SqlGenerator<AddColumnStatement> generatorUnderTest) throws Exception {
        super(generatorUnderTest);
    }

	@Override
	protected AddColumnStatement createSampleSqlStatement() {
		return new AddColumnStatement(null, null, null, null, null);
	}


	@Override
	protected List<? extends SqlStatement> setupStatements(
			Database database) {
		ArrayList<CreateTableStatement> statements = new ArrayList<CreateTableStatement>();
		CreateTableStatement table = new CreateTableStatement(null, TABLE_NAME);
		table
			.addColumn("id", "int", new NotNullConstraint());
		statements.add(table);
        
		if (database.supportsSchemas()) {
			table = new CreateTableStatement(TestContext.ALT_SCHEMA, TABLE_NAME);
			table
				.addColumn("id", "int", new NotNullConstraint());
			statements.add(table);
		}
		return statements;
	}

	@Override
    public void isValid() throws Exception {
        super.isValid();
        AddColumnStatement addPKColumn = new AddColumnStatement(null, null, null, null, null, new PrimaryKeyConstraint("pk_name"));

        assertFalse(generatorUnderTest.validate(addPKColumn, new OracleDatabase()).hasErrors());
        assertTrue(generatorUnderTest.validate(addPKColumn, new CacheDatabase()).getErrorMessages().contains("Cannot add a primary key column"));
        assertTrue(generatorUnderTest.validate(addPKColumn, new H2Database()).getErrorMessages().contains("Cannot add a primary key column"));
        assertTrue(generatorUnderTest.validate(addPKColumn, new DB2Database()).getErrorMessages().contains("Cannot add a primary key column"));
        assertTrue(generatorUnderTest.validate(addPKColumn, new DerbyDatabase()).getErrorMessages().contains("Cannot add a primary key column"));
        assertTrue(generatorUnderTest.validate(addPKColumn, new SQLiteDatabase()).getErrorMessages().contains("Cannot add a primary key column"));

        assertTrue(generatorUnderTest.validate(new AddColumnStatement(null, null, null, null, null, new AutoIncrementConstraint()), new MySQLDatabase()).getErrorMessages().contains("Cannot add a non-primary key identity column"));

        assertTrue(generatorUnderTest.validate(new AddColumnStatement(null, null, null, null, null, new AutoIncrementConstraint()), new MySQLDatabase()).getErrorMessages().contains("Cannot add a non-primary key identity column"));
    }

    @SuppressWarnings("unchecked")
	@Test
    public void generateSql_fullNoConstraints() throws Exception {
        AddColumnStatement statement = new AddColumnStatement(null, "table_name", "column_name", "int", 42);

        testSqlOnAllExcept("ALTER TABLE [table_name] ADD [column_name] int DEFAULT 42", statement, SybaseASADatabase.class, SybaseDatabase.class, MSSQLDatabase.class, SQLiteDatabase.class);
        testSqlOn("ALTER TABLE [table_name] ADD [column_name] INT NULL DEFAULT 42", statement, SybaseDatabase.class);
        testSqlOn("alter table [dbo].[table_name] add [column_name] int constraint df_table_name_column_name default 42", statement, MSSQLDatabase.class);
        testSqlOn("alter table [table_name] add [column_name] integer default 42", statement, SQLiteDatabase.class);
        testSqlOn("alter table table_name add column_name int default 42", statement, PostgresDatabase.class);
    }

    @SuppressWarnings("unchecked")
	@Test
    public void generateSql_autoIncrement() throws Exception {
        AddColumnStatement statement = new AddColumnStatement(null, "table_name", "column_name", "int", null, new AutoIncrementConstraint());

        testSqlOnAllExcept("ALTER TABLE [table_name] ADD [column_name] int auto_increment_clause", statement, SybaseASADatabase.class, SybaseDatabase.class, PostgresDatabase.class, MSSQLDatabase.class);
        testSqlOn("ALTER TABLE [dbo].[table_name] ADD [column_name] int auto_increment_clause", statement, MSSQLDatabase.class);
        testSqlOn("alter table [table_name] add [column_name] int default autoincrement null", statement, SybaseASADatabase.class);
        testSqlOn("alter table [table_name] add [column_name] int identity null", statement, SybaseDatabase.class);
        testSqlOn("alter table [table_name] add [column_name] serial", statement, PostgresDatabase.class);
     }

    @SuppressWarnings("unchecked")
	@Test
    public void generateSql_notNull() throws Exception {
        AddColumnStatement statement = new AddColumnStatement(null, "table_name", "column_name", "int", 42, new NotNullConstraint());

        testSqlOnAllExcept("ALTER TABLE [table_name] ADD [column_name] int NOT NULL DEFAULT 42", statement, SybaseASADatabase.class, MSSQLDatabase.class);
        testSqlOn("ALTER TABLE [table_name] ADD [column_name] int NOT NULL DEFAULT 42", statement, SybaseASADatabase.class);
        testSqlOn("alter table [dbo].[table_name] add [column_name] int not null constraint df_table_name_column_name default 42", statement, MSSQLDatabase.class);
    }

    @SuppressWarnings("unchecked")
	@Test
    public void generateSql_primaryKey() throws Exception {
    	AddColumnStatement statement = new AddColumnStatement(null, "table_name", "column_name", "int", null, new PrimaryKeyConstraint());
        testSqlOnAllExcept("ALTER TABLE [table_name] ADD [column_name] int NOT NULL PRIMARY KEY", statement
//        		sqlserver (at least 2000) does not allows add not null column.        
        		, MSSQLDatabase.class
        		);
        
    }

}