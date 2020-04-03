package liquibase.database.core;

import liquibase.database.DatabaseConnection;
import liquibase.exception.DatabaseException;

public class Firebird3Database extends FirebirdDatabase {

    @Override
    public boolean isCorrectDatabaseImplementation(DatabaseConnection conn) throws DatabaseException {
        return conn.getDatabaseProductName().startsWith("Firebird") && conn.getDatabaseMajorVersion() >= 3;
    }

    @Override
    public String getShortName() {
        return "firebird3";
    }
}
