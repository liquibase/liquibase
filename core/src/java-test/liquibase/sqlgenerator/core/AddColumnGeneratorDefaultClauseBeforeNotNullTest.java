package liquibase.sqlgenerator.core;

import static org.junit.Assert.assertTrue;
import liquibase.database.Database;
import liquibase.database.core.OracleDatabase;
import liquibase.database.core.DB2Database;
import liquibase.database.core.DerbyDatabase;
import liquibase.database.core.*;
import liquibase.exception.ValidationErrors;
import liquibase.statement.core.AddColumnStatement;
import liquibase.statement.AutoIncrementConstraint;
import liquibase.sqlgenerator.core.AddColumnGeneratorDefaultClauseBeforeNotNull;
import liquibase.sqlgenerator.MockSqlGeneratorChain;

import org.junit.Test;

public class AddColumnGeneratorDefaultClauseBeforeNotNullTest extends AddColumnGeneratorTest {
    public AddColumnGeneratorDefaultClauseBeforeNotNullTest() throws Exception {
        super(new AddColumnGeneratorDefaultClauseBeforeNotNull());
    }

    @Test
    public void validate_noAutoIncrementWithDerby() {
        ValidationErrors validationErrors = generatorUnderTest.validate(new AddColumnStatement(null, "table_name", "column_name", "int", null, new AutoIncrementConstraint("column_name")), new DerbyDatabase(), new MockSqlGeneratorChain());
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
