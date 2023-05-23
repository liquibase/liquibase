package liquibase.extension.testing.testsystem.core;

import liquibase.extension.testing.testsystem.DatabaseTestSystem;
import liquibase.extension.testing.testsystem.wrapper.DatabaseWrapper;
import liquibase.extension.testing.testsystem.wrapper.DockerDatabaseWrapper;
import org.jetbrains.annotations.NotNull;
import org.testcontainers.containers.Db2Container;
import org.testcontainers.utility.DockerImageName;

import java.util.Collections;

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
                new Db2Container(DockerImageName.parse(getImageName()).withTag("latest"))
                        .withUsername(getUsername())
                        .withPassword(getPassword())
                        .withDatabaseName(getCatalog())
                        .withTmpFs(Collections.singletonMap("/tmp", "rw"))
                        .withUrlParam("retrieveMessagesFromServerOnGetMessage", "true"),
                this
        ) {
            @Override
            public Runnable requireLicense() {
                return ((Db2Container) this.getContainer())::acceptLicense;
            }
        };
    }

    @Override
    protected String[] getSetupSql() {
        return new String[]{
            "CREATE TABLESPACE "+getAltTablespace()
        };
    }
}
