package liquibase.extension.testing.testsystem.core;

import liquibase.extension.testing.testsystem.DatabaseTestSystem;
import liquibase.extension.testing.testsystem.wrapper.DatabaseWrapper;
import liquibase.extension.testing.testsystem.wrapper.DockerDatabaseWrapper;
import org.firebirdsql.testcontainers.FirebirdContainer;
import org.testcontainers.utility.DockerImageName;

public class FirebirdTestSystem  extends DatabaseTestSystem {

    public FirebirdTestSystem() {
        super("firebird");
    }

    public FirebirdTestSystem(Definition definition) {
        super(definition);
    }

    @SuppressWarnings("java:S2095") // we can't close the wrapper as it will be used by the invoking method
    @Override
    protected DatabaseWrapper createContainerWrapper() throws Exception {
        return new DockerDatabaseWrapper(new FirebirdContainer(
                DockerImageName.parse(getImageName()).withTag(getVersion()))
                .withDatabaseName(getCatalog())
                .withUsername(getUsername())
                .withPassword(getPassword()),
                this
        );
    }

    @Override
    protected String[] getSetupSql() {
        return new String[0];
    }
}
