package liquibase.dbtest.snowflake;

import liquibase.database.DatabaseFactory;
import liquibase.dbtest.AbstractIntegrationTest;
import liquibase.exception.DatabaseException;
import org.junit.Ignore;
import org.junit.Test;

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
