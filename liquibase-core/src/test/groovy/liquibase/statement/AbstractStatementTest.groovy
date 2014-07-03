package liquibase.statement;

import liquibase.AbstractExtensibleObjectTest;
import liquibase.ExecutionEnvironment;
import liquibase.statement.Statement;
import liquibase.statementlogic.StatementLogicFactory;
import liquibase.database.Database;
import liquibase.test.TestContext;
import org.junit.Test;

import static org.junit.Assert.fail;

/**
 * Base class to use for all tests of Statement objects.
 */
public abstract class AbstractStatementTest<StatementUnderTest extends Statement> extends AbstractExtensibleObjectTest {

    def hasAtLeastOneGenerator() {
        expect:

        if (!expectAtLeastOneGenerator()) {
            return;
        }

        for (Database database : TestContext.getInstance().getAllDatabases()) {
            if (StatementLogicFactory.getInstance().supports(createObject(), new ExecutionEnvironment(database))) {
                return;
            };
        }
        fail("did not find a generator");
    }

    /**
     * Used by {@link #hasAtLeastOneGenerator()} to determine if test should run.
     */
    protected boolean expectAtLeastOneGenerator() {
        return true;
    }

    @Override
    protected List<String> getStandardProperties() {
        def properties = super.getStandardProperties()
        properties.remove("baseAffectedDatabaseObjects")
        properties.remove("affectedDatabaseObjects")

        if (properties.size() == 0) {
            properties.add("NONE")
        }

        return properties
    }
}
