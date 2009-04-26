package liquibase.database.statement.generator;

import org.junit.Test;
import static org.junit.Assert.*;
import liquibase.database.statement.AddColumnStatement;
import liquibase.database.statement.AutoIncrementConstraint;
import liquibase.database.statement.NotNullConstraint;
import liquibase.database.statement.PrimaryKeyConstraint;
import liquibase.database.*;

public class AddColumnGeneratorDefaultClauseBeforeNotNullTest extends AddColumnGeneratorTest {
    public AddColumnGeneratorDefaultClauseBeforeNotNullTest() {
        super(new AddColumnGeneratorDefaultClauseBeforeNotNull());
    }

    @Test
    public void generateSql_autoIncrement() throws Exception {
        AddColumnStatement statement = new AddColumnStatement(null, "table_name", "column_name", "int", null, new AutoIncrementConstraint("column_name"));

        testSqlOnAllExcept("alter table table_name add column_name int auto_increment_clause", statement, InformixDatabase.class);
        testSqlOn("alter table table_name add column_name serial", statement, InformixDatabase.class);
    }

    @Test
    public void generateSql_notNull() throws Exception {
        testSqlOnAll("ALTER TABLE [table_name] ADD [column_name] int DEFAULT 42 NOT NULL", new AddColumnStatement(null, "table_name", "column_name", "int", 42, new NotNullConstraint()));
    }

    @Test
    public void generateSql_primaryKey() throws Exception {
        AddColumnStatement statement = new AddColumnStatement(null, "table_name", "column_name", "int", null, new PrimaryKeyConstraint());
        
        testSqlOnAllExcept("ALTER TABLE [table_name] ADD [column_name] int PRIMARY KEY NOT NULL", statement, HsqlDatabase.class);
        testSqlOn("ALTER TABLE [table_name] ADD [column_name] INT NOT NULL PRIMARY KEY", statement, HsqlDatabase.class);
    }

    @Test
    public void validate_noAutoIncrementWithDerby() {
        GeneratorValidationErrors validationErrors = generatorUnderTest.validate(new AddColumnStatement(null, "table_name", "column_name", "int", null, new AutoIncrementConstraint("column_name")), new DerbyDatabase());
        assertTrue(validationErrors.getErrorMessages().contains("Cannot add an identity column to a database"));
    }

    @Override
    protected boolean shouldBeImplementation(Database database) {
        return database instanceof OracleDatabase
                || database instanceof HsqlDatabase
                || database instanceof DerbyDatabase
                || database instanceof DB2Database
                || database instanceof FirebirdDatabase
                || database instanceof InformixDatabase;
    }
}
