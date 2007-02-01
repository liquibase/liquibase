package liquibase.database.struture;

import java.sql.Connection;

public interface DatabaseStructure extends Comparable {
    public Connection getConnection();
}
