package liquibase.extension.testing.testsystem.core;

import liquibase.extension.testing.testsystem.DatabaseTestSystem;
import liquibase.extension.testing.testsystem.wrapper.DatabaseWrapper;
import liquibase.extension.testing.testsystem.wrapper.JdbcDatabaseWrapper;

/**
 * When running against Localstack Snowflake, you may need to add the following to your run configuration:
 * <code>--add-opens=java.base/java.nio=ALL-UNNAMED</code>. See more information
 * <a href="https://arrow.apache.org/docs/java/install.html#java-compatibility">here</a>.
 */
public class SnowflakeTestSystem extends DatabaseTestSystem {
    public SnowflakeTestSystem() {
        super("snowflake");
    }

    public SnowflakeTestSystem(Definition definition) {
        super(definition);
    }

    @Override
    protected DatabaseWrapper createContainerWrapper() throws Exception {
        return new JdbcDatabaseWrapper(getConnectionUrl(), getUsername(), getPassword());
    }

    @Override
    protected String[] getSetupSql() {
        return new String[]{
                "create schema " + getAltSchema(),
                "grant all on schema " + getAltSchema() + " to " + getUsername(),
        };
    }
}
