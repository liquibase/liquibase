package liquibase.extension.testing.testsystem.core;

import liquibase.extension.testing.testsystem.DatabaseTestSystem;
import liquibase.extension.testing.testsystem.wrapper.DatabaseWrapper;
import liquibase.extension.testing.testsystem.wrapper.DockerDatabaseWrapper;
import org.jetbrains.annotations.NotNull;
import org.testcontainers.containers.Db2Container;
import org.testcontainers.utility.DockerImageName;

public class DB2TestSystem extends DatabaseTestSystem {

    public DB2TestSystem() {
        super("db2");
    }

    public DB2TestSystem(Definition definition) {
        super(definition);
    }

    @SuppressWarnings("java:S2095")
    @Override
    protected @NotNull DatabaseWrapper createContainerWrapper() {
        return new DockerDatabaseWrapper(
                new Db2Container(DockerImageName.parse(getImageName()).withTag(getVersion()))
                        .withUsername(getUsername())
                        .withPassword(getPassword())
                        .withDatabaseName(getCatalog())
                        .acceptLicense()
                        .withUrlParam("retrieveMessagesFromServerOnGetMessage", "true"),
                this
        );
    }

    @Override
    protected String[] getSetupSql() {
        return new String[]{
            "CREATE TABLESPACE "+getAltTablespace()
        };
    }
}
