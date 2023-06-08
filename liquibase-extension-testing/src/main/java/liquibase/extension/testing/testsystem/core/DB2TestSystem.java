package liquibase.extension.testing.testsystem.core;

import liquibase.extension.testing.testsystem.DatabaseTestSystem;
import liquibase.extension.testing.testsystem.TestSystem;
import liquibase.extension.testing.testsystem.wrapper.DatabaseWrapper;
import liquibase.extension.testing.testsystem.wrapper.DockerDatabaseWrapper;
import org.jetbrains.annotations.NotNull;
import org.testcontainers.containers.Db2Container;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class DB2TestSystem extends TestSystem {

    private final String db2NetworkAlias = "db2database";

    private final GenericContainer<?> db2 = new GenericContainer<>("ibmcom/db2")
            .withEnv("LICENSE", "accept")
            .withEnv("DB2INST1_PASSWORD", "choose")
            .withEnv("DBNAME", "testdb")
//            .withNetwork(network)
            .withNetworkAliases(db2NetworkAlias)
            .withExposedPorts(50000, 55000)
            .withFileSystemBind(System.getProperty("java.io.tmpdir"), "/database")
            .waitingFor(new LogMessageWaitStrategy().withRegEx(".*Setup has completed.*").withStartupTimeout(Duration.ofMinutes(10)))
            .withCommand("db2start")
            .withPrivilegedMode(true)
            .withReuse(true); // It is necessary to have this set to true, because the stop method will stop the containers if necessary.


    public DB2TestSystem() {
        super("db2");
    }

    public DB2TestSystem(Definition definition) {
        super(definition);
    }

    @Override
    public void start() throws Exception {
        db2.start();
    }

    @Override
    public void stop() throws Exception {
        if(!getKeepRunning()) {
            db2.stop();
        }
    }

    public String getUrl() {
        return "jdbc:db2://localhost:"+db2.getMappedPort(50000)+"/testdb";
    }

    public String getPassword() {
        return "choose";
    }

    public String getUsername() {
        return "choose";
    }

//
//    @SuppressWarnings("java:S2095")
//    @Override
//    protected @NotNull DatabaseWrapper createContainerWrapper() {
//        return new DockerDatabaseWrapper(
//                new Db2Container(DockerImageName.parse(getImageName()).withTag(getVersion()))
//                        .withUsername(getUsername())
//                        .withPassword(getPassword())
//                        .withDatabaseName(getCatalog())
//                        .withUrlParam("retrieveMessagesFromServerOnGetMessage", "true"),
//                this
//        ) {
//            @Override
//            public Runnable requireLicense() {
//                return ((Db2Container) this.getContainer())::acceptLicense;
//            }
//        };
//    }
//
//    @Override
//    protected String[] getSetupSql() {
//        return new String[]{
//            "CREATE TABLESPACE "+getAltTablespace()
//        };
//    }
}
