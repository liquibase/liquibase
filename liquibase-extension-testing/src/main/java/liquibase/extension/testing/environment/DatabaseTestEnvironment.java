package liquibase.extension.testing.environment;

import java.sql.SQLException;

public interface DatabaseTestEnvironment {

    void createDefaultSchema() throws SQLException;
    void createAltSchema() throws SQLException;

    void createDefaultCatalog();
    void createAltCatalog() throws SQLException;
}
