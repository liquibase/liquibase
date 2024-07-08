package liquibase.extension.testing.testsystem.core;

import liquibase.extension.testing.testsystem.DatabaseTestSystem;
import liquibase.extension.testing.testsystem.wrapper.DatabaseWrapper;
import liquibase.extension.testing.testsystem.wrapper.DockerDatabaseWrapper;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.utility.DockerImageName;

public class MariaDBTestSystem extends DatabaseTestSystem {

    public MariaDBTestSystem() {
        super("mariadb");
    }

    public MariaDBTestSystem(Definition definition) {
        super(definition);
    }

    @SuppressWarnings("java:S2095")
    @Override
    protected DatabaseWrapper createContainerWrapper() throws Exception {
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
                "create database if not exists " + getAltSchema(), //create altSchema as a catalog since some integration tests don't handle the difference
                "GRANT ALL PRIVILEGES ON " + getCatalog() + ".* TO '" + getUsername() + "'@'%'",
                "GRANT ALL PRIVILEGES ON " + getAltCatalog() + ".* TO '" + getUsername() + "'@'%'",
                "GRANT ALL PRIVILEGES ON " + getAltSchema() + ".* TO '" + getUsername() + "'@'%'"
        };
    }
}
