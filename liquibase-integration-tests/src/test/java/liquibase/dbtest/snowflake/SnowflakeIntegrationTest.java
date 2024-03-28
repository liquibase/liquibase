package liquibase.dbtest.snowflake;

import liquibase.database.DatabaseFactory;
import liquibase.dbtest.AbstractIntegrationTest;
import liquibase.exception.DatabaseException;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Note, to run this in Intellij, you may need to add the following to your run configuration:
 * <code>--add-opens=java.base/java.nio=ALL-UNNAMED</code>. See more information
 * <a href="https://arrow.apache.org/docs/java/install.html#java-compatibility">here</a>.
 */
@Ignore("The localstack Snowflake integration is not mature enough to support the test framework. See DAT-17037.")
public class SnowflakeIntegrationTest extends AbstractIntegrationTest {

    public SnowflakeIntegrationTest() throws Exception {
        super("snowflake", DatabaseFactory.getInstance().getDatabase("snowflake"));
    }

    @Ignore(value = "Snowflake does not support indexes.")
    @Override
    @Test
    public void verifyIndexIsCreatedWhenAssociatedWithPropertyIsSetAsNone() throws DatabaseException {
        super.verifyIndexIsCreatedWhenAssociatedWithPropertyIsSetAsNone();
    }
}
