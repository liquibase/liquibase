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
//
//    public String getUrl() {
//        return "jdbc:db2://localhost:"+db2.getMappedPort(50000)+"/testdb";
//    }


    @SuppressWarnings("java:S2095")
    @Override
    protected @NotNull DatabaseWrapper createContainerWrapper() {

        db2.start();
        return new DockerDatabaseWrapper(
                db2
                , this
        );
    }

    @Override
    protected String[] getSetupSql() {
        return new String[]{
            "CREATE TABLESPACE "+getAltTablespace()
        };
    }
}