package liquibase.extension.testing.testsystem.core;

import liquibase.extension.testing.testsystem.DatabaseTestSystem;
import liquibase.extension.testing.testsystem.wrapper.DatabaseWrapper;
import liquibase.extension.testing.testsystem.wrapper.JdbcDatabaseWrapper;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

public class H2TestSystem extends DatabaseTestSystem {

    public H2TestSystem() {
        super("h2");
    }

    public H2TestSystem(Definition definition) {
        super(definition);
    }

    @Override
    public String getDriverJar() {
        String driver = super.getDriverJar();
        if (driver == null) {
            final String version = getVersion();
            if (version != null) {
                driver = "com.h2database:h2:"+version;
            }
        }

        return driver;
    }

    @Override
    protected DatabaseWrapper createContainerWrapper() throws Exception {
        throw new IllegalArgumentException("Cannot create container for h2. Use url");
    }

    @Override
    protected String[] getSetupSql() {
        return new String[]{
                "create schema " + getAltSchema(),
        };
    }
}
