package liquibase.database.struture;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseSystem implements DatabaseStructure {

    private List<Table> tables;
    private Connection conn;
    private String dbInfo;

    public DatabaseSystem(Connection conn) throws SQLException {
        this.conn = conn;

        this.tables = new ArrayList<Table>();
        dbInfo = conn.getMetaData().getURL();
    }

    public List<Table> getTables() {
        return tables;
    }

    public String toString() {
        return dbInfo;
    }

    public int compareTo(Object o) {
        if (o instanceof DatabaseSystem) {
            return toString().compareTo(o.toString());
        } else {
            return getClass().getName().compareTo(o.getClass().getName());
        }
    }

    public Connection getConnection() {
        return conn;
    }

}
