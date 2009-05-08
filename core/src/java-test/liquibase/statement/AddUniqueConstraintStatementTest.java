package liquibase.statement;

import liquibase.database.Database;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.test.DatabaseTestTemplate;
import liquibase.test.SqlStatementDatabaseTest;
import liquibase.test.TestContext;
import static org.junit.Assert.assertFalse;
import org.junit.Test;

public class AddUniqueConstraintStatementTest extends AbstractSqStatementTest<AddUniqueConstraintStatement> {

    protected AddUniqueConstraintStatement createStatementUnderTest() {
        return new AddUniqueConstraintStatement(null, null, null, null);
    }


}
