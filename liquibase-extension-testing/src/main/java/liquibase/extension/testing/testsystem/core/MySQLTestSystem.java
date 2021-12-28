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

    @Override
    protected @NotNull DatabaseWrapper createWrapper() {
        return new DockerDatabaseWrapper(
                new MySQLContainer(DockerImageName.parse(getImageName()).withTag(getVersion()))
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
                "GRANT ALL PRIVILEGES ON " + getCatalog() + ".* TO '" + getUsername() + "'@'%'",
                "GRANT ALL PRIVILEGES ON " + getAltCatalog() + ".* TO '" + getUsername() + "'@'%'"
        };
    }
}
