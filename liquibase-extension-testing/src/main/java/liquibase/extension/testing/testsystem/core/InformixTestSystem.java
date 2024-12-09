package liquibase.extension.testing.testsystem.core;

import liquibase.extension.testing.testsystem.DatabaseTestSystem;
import liquibase.extension.testing.testsystem.InformixContainer;
import liquibase.extension.testing.testsystem.wrapper.DatabaseWrapper;
import liquibase.extension.testing.testsystem.wrapper.DockerDatabaseWrapper;
import org.jetbrains.annotations.NotNull;
import org.testcontainers.utility.DockerImageName;

public class InformixTestSystem  extends DatabaseTestSystem {

    public InformixTestSystem() {
        super("informix");
    }

    public InformixTestSystem(Definition definition) {
        super(definition);
    }

    @Override
    protected @NotNull DatabaseWrapper createContainerWrapper() {
        return new DockerDatabaseWrapper(
                new InformixContainer(DockerImageName.parse(getImageName()).withTag(getVersion())),
                this
        );
    }

    @Override
    protected String[] getSetupSql() {
        return new String[0];
    }
}
