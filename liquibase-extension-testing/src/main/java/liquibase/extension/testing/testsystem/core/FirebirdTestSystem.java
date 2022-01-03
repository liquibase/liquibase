package liquibase.extension.testing.testsystem.core;

import liquibase.extension.testing.testsystem.DatabaseTestSystem;
import liquibase.extension.testing.testsystem.wrapper.DatabaseWrapper;
import liquibase.extension.testing.testsystem.wrapper.UnimplementedWrapper;
import org.jetbrains.annotations.NotNull;

public class FirebirdTestSystem  extends DatabaseTestSystem {

    public FirebirdTestSystem() {
        super("firebird");
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
