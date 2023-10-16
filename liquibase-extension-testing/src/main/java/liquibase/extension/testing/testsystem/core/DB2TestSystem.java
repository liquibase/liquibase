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
//
//    public String getUrl() {
//        return "jdbc:db2://localhost:"+db2.getMappedPort(50000)+"/testdb";
//    }


    @SuppressWarnings("java:S2095")
    @Override
    protected @NotNull DatabaseWrapper createContainerWrapper() {

//         new GenericContainer<>("ibmcom/db2")
//                .withEnv("LICENSE", "accept")
//                .withEnv("DB2INST1_PASSWORD", getPassword())
//                .withEnv("DBNAME", "testdb")
////            .withNetwork(network)
//                .withNetworkAliases(db2NetworkAlias)
//                .withExposedPorts(50000, 55000)
//                //.withFileSystemBind(System.getProperty("java.io.tmpdir"), "/database")
//                .waitingFor(new LogMessageWaitStrategy().withRegEx(".*Setup has completed.*").withStartupTimeout(Duration.ofMinutes(10)))
//                .withCommand("db2start")
//                .withPrivilegedMode(true)
//                .withReuse(true); // It is necessary to have this set to true, because the stop method will stop the containers if necessary.


        return new DockerDatabaseWrapper(
                new Db2Container(DockerImageName.parse(getImageName()).withTag(getVersion()))
                        .withUsername(getUsername())
                        .withPassword(getPassword())
                        .withDatabaseName(getCatalog())
                        .acceptLicense()
                        .withPrivilegedMode(true)
                        .withReuse(true)
                , this
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