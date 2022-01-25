package liquibase.extension.testing.testsystem.core;

import liquibase.extension.testing.testsystem.DatabaseTestSystem;
import liquibase.extension.testing.testsystem.wrapper.DatabaseWrapper;
import liquibase.extension.testing.testsystem.wrapper.DockerDatabaseWrapper;
import org.jetbrains.annotations.NotNull;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

public class MySQLTestSystem extends DatabaseTestSystem {

    public MySQLTestSystem() {
        super("mysql");
    }

    public MySQLTestSystem(Definition definition) {
        super(definition);
    }

    @SuppressWarnings("java:S2095")
    @Override
    protected @NotNull DatabaseWrapper createContainerWrapper() {
        return new DockerDatabaseWrapper(
                new MySQLContainer(DockerImageName.parse(getImageName()).withTag(getVersion()))
                        .withUsername(getUsername())
                        .withPassword(getPassword())
                        .withDatabaseName(getCatalog())
                        .withUrlParam("useSSL", "false")
                        .withUrlParam("allowPublicKeyRetrieval", "true"),
                this
        );
    }

    @Override
    protected String[] getSetupSql() {
        return new String[]{
                "create database if not exists " + getAltCatalog(),
                "create database if not exists " + getAltSchema(), //create altSchema as a catalog since some integration tests don't handle the difference
                "GRANT ALL PRIVILEGES ON " + getCatalog() + ".* TO '" + getUsername() + "'@'%'",
                "GRANT ALL PRIVILEGES ON " + getAltCatalog() + ".* TO '" + getUsername() + "'@'%'",
                "GRANT ALL PRIVILEGES ON " + getAltSchema() + ".* TO '" + getUsername() + "'@'%'"
        };
    }
}
