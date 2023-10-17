package liquibase.extension.testing.testsystem.core;

import liquibase.extension.testing.testsystem.DatabaseTestSystem;
import liquibase.extension.testing.testsystem.wrapper.DatabaseWrapper;
import liquibase.extension.testing.testsystem.wrapper.DockerDatabaseWrapper;
import org.jetbrains.annotations.NotNull;
import org.testcontainers.containers.Db2Container;
import org.testcontainers.utility.DockerImageName;

public class DB2TestSystem extends DatabaseTestSystem {

    private Db2Container db2 = new Db2Container(DockerImageName.parse(getImageName())
            .withTag(getVersion()))
            .withUsername(getUsername())
            .withPassword(getPassword())
            .withDatabaseName(getCatalog())
            .acceptLicense();

    public DB2TestSystem() {
        super("db2");
    }

    public DB2TestSystem(Definition definition) {
        super(definition);
    }

    @Override
    public void start() throws Exception {
        if (db2.isRunning()) {
            return;
        }
        // overriding db2 container start here, as using start from parent command calls exec on the docker containers
        // and for some unknown reason db2 does not start the tcpip listener. It causes tests to fail with error:
        // Reply.fill() - insufficient data (-1).  Message: Insuficient data. ERRORCODE=-4499, SQLSTATE=08001
        db2.start();
        super.start();
    }

    @SuppressWarnings("java:S2095")
    @Override
    protected @NotNull DatabaseWrapper createContainerWrapper() {
        return new DockerDatabaseWrapper(db2, this);
    }

    @Override
    protected String[] getSetupSql() {
        return new String[]{
            "CREATE TABLESPACE "+getAltTablespace()
        };
    }
}