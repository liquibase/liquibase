package liquibase.extension.testing.testsystem.core;

import liquibase.extension.testing.testsystem.DatabaseTestSystem;
import liquibase.extension.testing.testsystem.wrapper.DatabaseWrapper;
import liquibase.extension.testing.testsystem.wrapper.UnimplementedWrapper;

public class InformixTestSystem  extends DatabaseTestSystem {

    public InformixTestSystem() {
        super("informix");
    }

    public InformixTestSystem(Definition definition) {
        super(definition);
    }

    @Override
    protected DatabaseWrapper createContainerWrapper() throws Exception {
        return new UnimplementedWrapper();
    }

    @Override
    protected String[] getSetupSql() {
        return new String[0];
    }
}
