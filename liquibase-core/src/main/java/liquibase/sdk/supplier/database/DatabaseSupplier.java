package liquibase.sdk.supplier.database;

import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.OfflineConnection;
import liquibase.database.jvm.JdbcConnection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class DatabaseSupplier {

    private HashSet<Database> allDatabases;

    public Set<Database> getAllDatabases() {
        if (allDatabases == null) {
            allDatabases = new HashSet<Database>(DatabaseFactory.getInstance().getImplementedDatabases());

            for (Database database : allDatabases) {
                ConnectionConfigurationFactory configurationFactory = ConnectionConfigurationFactory.getInstance();
                for (ConnectionSupplier connectionSupplier : configurationFactory.getConfigurations(database)) {
                    try {
                        Connection connection = connectionSupplier.openConnection();
                        database.setConnection(new JdbcConnection(connection));
                        break;
                    } catch (SQLException e) {
                        System.out.println("Cannot connect to "+connectionSupplier.getJdbcUrl());
                        database.setConnection(new OfflineConnection("offline:"+database.getShortName()));
                    }
                }
            }
        }
        return allDatabases;
    }
}
