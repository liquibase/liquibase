package liquibase.extension.testing.testsystem.core;

import liquibase.extension.testing.testsystem.DatabaseTestSystem;
import liquibase.extension.testing.testsystem.wrapper.DatabaseWrapper;
import liquibase.extension.testing.testsystem.wrapper.JdbcDatabaseWrapper;

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
