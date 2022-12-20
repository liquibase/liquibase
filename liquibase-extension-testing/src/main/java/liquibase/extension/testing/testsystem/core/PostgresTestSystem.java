package liquibase.extension.testing.testsystem.core;

import liquibase.extension.testing.testsystem.DatabaseTestSystem;
import liquibase.extension.testing.testsystem.wrapper.DatabaseWrapper;
import liquibase.extension.testing.testsystem.wrapper.DockerDatabaseWrapper;
import org.jetbrains.annotations.NotNull;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public class PostgresTestSystem extends DatabaseTestSystem {

    public PostgresTestSystem() {
        super("postgresql");
    }

    public PostgresTestSystem(Definition definition) {
        super(definition);
    }

    @SuppressWarnings("java:S2095")
    @Override
    protected @NotNull DatabaseWrapper createContainerWrapper() {
        return new DockerDatabaseWrapper(new PostgreSQLContainer(
                DockerImageName.parse(getImageName()).withTag(getVersion()))
                .withUsername(getUsername())
                .withPassword(getPassword())
                .withDatabaseName(getCatalog()),
                this
        );
    }

    @Override
    protected String[] getSetupSql() {
        return new String[]{
                "CREATE DATABASE " + getAltCatalog(),
                "CREATE SCHEMA IF NOT EXISTS " + getAltSchema(),
                "COPY (SELECT 1) TO PROGRAM 'mkdir -p /tmp/" + getAltTablespace() + "'",
                "CREATE TABLESPACE " + getAltTablespace() + " OWNER lbuser LOCATION '/tmp/" + getAltTablespace() + "'",
                "GRANT ALL PRIVILEGES ON DATABASE " + getCatalog() + " TO " + getUsername(),
                "GRANT ALL PRIVILEGES ON DATABASE " + getAltCatalog() + " TO " + getUsername(),
                "GRANT ALL PRIVILEGES ON SCHEMA " + getAltSchema() + " TO " + getUsername()
        };
    }

}
