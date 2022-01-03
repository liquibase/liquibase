package liquibase.extension.testing.testsystem.core;

import liquibase.extension.testing.testsystem.DatabaseTestSystem;
import liquibase.extension.testing.testsystem.wrapper.DatabaseWrapper;
import liquibase.extension.testing.testsystem.wrapper.UnimplementedWrapper;
import org.jetbrains.annotations.NotNull;

public class InformixTestSystem  extends DatabaseTestSystem {

    public InformixTestSystem() {
        super("informix");
    }

    @Override
    protected @NotNull DatabaseWrapper createWrapper() throws Exception {
        return new UnimplementedWrapper();
    }

    @Override
    protected String[] getSetupSql() {
        return new String[0];
    }
}
