package liquibase.statement;

import liquibase.database.Database;
import liquibase.database.PostgresDatabase;
import liquibase.database.template.Executor;
import liquibase.exception.JDBCException;
import liquibase.statement.generator.SqlGenerator;
import liquibase.statement.generator.SqlGeneratorFactory;
import liquibase.statement.*;
import liquibase.test.TestContext;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;
import static org.junit.Assert.*;

public abstract class AbstractSqStatementTest<SqlStatementUnderTest extends SqlStatement> {

    protected abstract SqlStatementUnderTest createStatementUnderTest();

    @Test
    public void hasAtLeastOneGenerator() {
        for (Database database : TestContext.getInstance().getAllDatabases()) {
            if (SqlGeneratorFactory.getInstance().getBestGenerator(createStatementUnderTest(), database) != null) {
                return;
            };
        }
        fail("did not find a generator");
    }

}
