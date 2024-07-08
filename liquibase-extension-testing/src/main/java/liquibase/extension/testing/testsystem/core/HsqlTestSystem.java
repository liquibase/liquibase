package liquibase.extension.testing.testsystem.core;

import liquibase.extension.testing.testsystem.DatabaseTestSystem;
import liquibase.extension.testing.testsystem.wrapper.DatabaseWrapper;

public class HsqlTestSystem extends DatabaseTestSystem {

     public HsqlTestSystem() {
        super("hsqldb");
    }

    public HsqlTestSystem(Definition definition) {
        super(definition);
    }

    @Override
    protected DatabaseWrapper createContainerWrapper() throws Exception {
        throw new IllegalArgumentException("Cannot create container for hsql. Use url");
    }

    @Override
    protected String[] getSetupSql() {
        return new String[] {
                "create schema "+getAltSchema(),
        };
    }

}
