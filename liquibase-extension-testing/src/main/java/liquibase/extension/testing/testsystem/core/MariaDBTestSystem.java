package liquibase.extension.testing.testsystem.core;

import liquibase.extension.testing.testsystem.DatabaseTestSystem;
import liquibase.extension.testing.testsystem.wrapper.DatabaseWrapper;
import liquibase.extension.testing.testsystem.wrapper.DockerDatabaseWrapper;
import org.jetbrains.annotations.NotNull;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

public class MariaDBTestSystem extends DatabaseTestSystem {

    public MariaDBTestSystem() {
        super("mariadb");
    }

    @Override
    protected @NotNull DatabaseWrapper createWrapper() {
        return new DockerDatabaseWrapper(
                new MariaDBContainer(DockerImageName.parse(getImageName()).withTag(getVersion()))
                        .withUsername(getUsername())
                        .withPassword(getPassword())
                        .withDatabaseName(getCatalog()),
                this
        );
    }

    @Override
    protected String[] getSetupSql() {
        return new String[]{
                "create database if not exists " + getAltCatalog(),
                "create schema if not exists " + getAltSchema(),
                "GRANT ALL PRIVILEGES ON " + getCatalog() + ".* TO '" + getUsername() + "'@'%'",
                "GRANT ALL PRIVILEGES ON " + getAltCatalog() + ".* TO '" + getUsername() + "'@'%'"
        };
    }
}
