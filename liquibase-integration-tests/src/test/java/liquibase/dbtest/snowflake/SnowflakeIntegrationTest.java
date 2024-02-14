package liquibase.dbtest.snowflake;

import liquibase.database.DatabaseFactory;
import liquibase.dbtest.AbstractIntegrationTest;

public class SnowflakeIntegrationTest extends AbstractIntegrationTest {

    public SnowflakeIntegrationTest() throws Exception {
        super("snowflake", DatabaseFactory.getInstance().getDatabase("snowflake"));
    }
}
