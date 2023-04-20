package liquibase.sqlgenerator.core;

import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.exception.ValidationErrors;
import liquibase.sqlgenerator.MockSqlGeneratorChain;
import liquibase.statement.AutoIncrementConstraint;
import liquibase.statement.core.AddColumnStatement;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class AddColumnGeneratorDefaultClauseBeforeNotNullTest extends AddColumnGeneratorTest {
    public AddColumnGeneratorDefaultClauseBeforeNotNullTest() throws Exception {
        super(new AddColumnGeneratorDefaultClauseBeforeNotNull());
    }

    @Test
    public void validate_noAutoIncrementWithDerby() {
        ValidationErrors validationErrors = generatorUnderTest.validate(new AddColumnStatement(null, null, "table_name", "column_name", "int", null, new AutoIncrementConstraint("column_name")), new DerbyDatabase(), new MockSqlGeneratorChain());
        assertTrue(validationErrors.getErrorMessages().contains("Cannot add an identity column to derby"));
    }

    @Override
    protected boolean shouldBeImplementation(Database database) {
        return (database instanceof OracleDatabase) || (database instanceof HsqlDatabase) || (database instanceof
            H2Database) || (database instanceof DerbyDatabase) || (database instanceof AbstractDb2Database) || (database
            instanceof FirebirdDatabase) || (database instanceof SybaseASADatabase) || (database instanceof
            SybaseDatabase) || (database instanceof InformixDatabase);
    }
}
