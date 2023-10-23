package liquibase.extension.testing.testsystem.core;

import liquibase.extension.testing.testsystem.DatabaseTestSystem;
import liquibase.extension.testing.testsystem.wrapper.DatabaseWrapper;
import org.jetbrains.annotations.NotNull;

public class SQLiteTestSystem extends DatabaseTestSystem {

    public SQLiteTestSystem() {
        super("sqlite");
    }

    public SQLiteTestSystem(Definition definition) {
        super(definition);
    }

    @Override
    protected @NotNull DatabaseWrapper createContainerWrapper() throws Exception {
        throw new IllegalArgumentException("Cannot create sqlite container. Use url");
    }

    @Override
    protected String[] getSetupSql() {
        return new String[]{
//                "create schema "+getAltSchema(),
        };
    }
}
