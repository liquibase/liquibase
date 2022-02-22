package liquibase.extension.testing.testsystem.core;

import liquibase.extension.testing.testsystem.DatabaseTestSystem;
import liquibase.extension.testing.testsystem.wrapper.DatabaseWrapper;
import liquibase.extension.testing.testsystem.wrapper.DockerDatabaseWrapper;
import org.jetbrains.annotations.NotNull;
import org.testcontainers.containers.OracleContainer;
import org.testcontainers.utility.DockerImageName;

public class OracleTestSystem extends DatabaseTestSystem {

    public OracleTestSystem() {
        super("oracle");
    }

    public OracleTestSystem(Definition definition) {
        super(definition);
    }

    @SuppressWarnings("java:S2095")
    @Override
    protected @NotNull DatabaseWrapper createContainerWrapper() {
        return new DockerDatabaseWrapper(
                new OracleContainer(DockerImageName.parse(getImageName()).withTag(getVersion()))
                        .withUsername(getUsername())
                        .withPassword(getPassword())
                        .withDatabaseName(getCatalog()),
                this
        );
    }

    @Override
    protected String[] getSetupSql() {
        String accountInfo = " DEFAULT TABLESPACE USERS TEMPORARY TABLESPACE TEMP ACCOUNT UNLOCK";
        return new String[]{
                "CREATE TABLESPACE " + getAltTablespace() + " DATAFILE '" + getAltTablespace() + ".dat' SIZE 1M AUTOEXTEND ON",
                "CREATE USER "+getAltCatalog()+" IDENTIFIED BY "+getPassword()+ accountInfo,
                "CREATE USER "+getAltSchema()+" IDENTIFIED BY "+getPassword()+ accountInfo,
                "CREATE USER LIMITED_USER IDENTIFIED BY "+getPassword()+ accountInfo,
                "GRANT CREATE SESSION TO LIMITED_USER",
                "GRANT CONNECT TO LIMITED_USER",
                "GRANT SELECT ON DUAL TO LIMITED_USER",
                "GRANT ALL PRIVILEGES TO "+getUsername(),
                "GRANT UNLIMITED TABLESPACE TO " + getUsername(),
                "GRANT UNLIMITED TABLESPACE TO " + getAltCatalog()
        };
    }
}
