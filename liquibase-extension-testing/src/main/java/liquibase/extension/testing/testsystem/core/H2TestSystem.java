package liquibase.extension.testing.testsystem.core;

import liquibase.extension.testing.testsystem.DatabaseTestSystem;
import liquibase.extension.testing.testsystem.wrapper.DatabaseWrapper;
import liquibase.extension.testing.testsystem.wrapper.JdbcDatabaseWrapper;
import org.jetbrains.annotations.NotNull;

public class H2TestSystem extends DatabaseTestSystem {

     public H2TestSystem() {
        super("h2");
    }

    @Override
    protected @NotNull DatabaseWrapper createWrapper() throws Exception{
        return new JdbcDatabaseWrapper("jdbc:h2:mem:"+ getTestSystemProperty("catalog", String.class), getUsername(), getPassword());
    }

    @Override
    protected String[] getSetupSql() {
        return new String[] {
                "create schema "+getAltSchema(),
        };
    }
}
