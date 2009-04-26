package liquibase.database.statement.generator;

import liquibase.database.statement.*;
import liquibase.database.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class AddColumnGeneratorTest extends AbstractSqlGeneratorTest {

    public AddColumnGeneratorTest() {
        this(new AddColumnGenerator());
    }

    public AddColumnGeneratorTest(SqlGenerator generatorUnderTest) {
        super(generatorUnderTest);
    }

    protected SqlStatement[] setupStatements() {
        return new SqlStatement[] {
            new CreateTableStatement(null, "table_name")
                .addColumn("id", "int")    
        };
    }

    protected AddColumnStatement createSampleSqlStatement() {
        return new AddColumnStatement(null, "table_name", "column_name", "int", 42);
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

    @Test
    public void generateSql_fullNoConstraints() throws Exception {
        AddColumnStatement statement = new AddColumnStatement(null, "table_name", "column_name", "int", 42);

        testSqlOnAllExcept("ALTER TABLE [table_name] ADD [column_name] int DEFAULT 42", statement, SybaseASADatabase.class, SybaseDatabase.class, MSSQLDatabase.class, SQLiteDatabase.class);
        testSqlOn("ALTER TABLE [table_name] ADD [column_name] INT NULL DEFAULT 42", statement, SybaseDatabase.class);
        testSqlOn("alter table [table_name] add [column_name] int constraint df_table_name_column_name default 42", statement, MSSQLDatabase.class);
        testSqlOn("alter table [table_name] add [column_name] integer default 42", statement, SQLiteDatabase.class);
        testSqlOn("alter table table_name add column_name int default 42", statement, PostgresDatabase.class);
    }

    @Test
    public void generateSql_autoIncrement() throws Exception {
        AddColumnStatement statement = new AddColumnStatement(null, "table_name", "column_name", "int", null, new AutoIncrementConstraint());

        testSqlOnAllExcept("ALTER TABLE [table_name] ADD [column_name] int auto_increment_clause", statement, SybaseASADatabase.class, SybaseDatabase.class, PostgresDatabase.class);
        testSqlOn("alter table [table_name] add [column_name] int default autoincrement null", statement, SybaseASADatabase.class);
        testSqlOn("alter table [table_name] add [column_name] int identity null", statement, SybaseDatabase.class);
        testSqlOn("alter table [table_name] add [column_name] serial", statement, PostgresDatabase.class);
     }

    @Test
    public void generateSql_notNull() throws Exception {
        AddColumnStatement statement = new AddColumnStatement(null, "table_name", "column_name", "int", 42, new NotNullConstraint());

        testSqlOnAllExcept("ALTER TABLE [table_name] ADD [column_name] int NOT NULL DEFAULT 42", statement, SybaseASADatabase.class, MSSQLDatabase.class);
        testSqlOn("ALTER TABLE [table_name] ADD [column_name] int NOT NULL DEFAULT 42", statement, SybaseASADatabase.class);
        testSqlOn("alter table [table_name] add [column_name] int not null constraint df_table_name_column_name default 42", statement, MSSQLDatabase.class);
    }

    @Test
    public void generateSql_primaryKey() throws Exception {
        testSqlOnAll("ALTER TABLE [table_name] ADD [column_name] int NOT NULL PRIMARY KEY", new AddColumnStatement(null, "table_name", "column_name", "int", null, new PrimaryKeyConstraint()));
    }
}