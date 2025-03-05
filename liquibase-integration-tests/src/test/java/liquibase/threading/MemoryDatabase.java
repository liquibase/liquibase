package liquibase.threading;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author github.com/bvremmeinfor
 */
public class MemoryDatabase implements AutoCloseable {

    private static final String SENSING_TABLE = "_fake_lock";

    private final String connectionUrl;
    private final Connection mainConnection;

    private boolean closed = false;

    private MemoryDatabase(String connectionUrl, Connection mainConnection) {
        this.connectionUrl = connectionUrl;
        this.mainConnection = mainConnection;
    }

    public static synchronized MemoryDatabase create(String dbName) {
        MemoryDatabase db = createUnvalidated(dbName);

        if (db.hasSensingTable()) {
            db.close();
            throw new IllegalStateException("Database already exists: " + dbName);
        }

        db.createSensingTable();

        return db;
    }

    public static boolean databaseExists(String dbName) {
        try (MemoryDatabase db = createUnvalidated(dbName)) {
            return db.hasSensingTable();
        }
    }

    public synchronized boolean isClosed() {
        return closed;
    }

    @Override
    public synchronized void close() {
        closed = true;
        try {
            mainConnection.close();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public synchronized Connection getConnection() {
        if (closed) {
            throw new IllegalStateException("Fake connection factory is closed!");
        }

        try {
            return createConnection(this.connectionUrl);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public List<Map<String, String>> queryConnections() {
        return query("select * from information_schema.sessions");
    }

    public boolean hasTable(String tableName) {
        final String lowercaseTableName = tableName.toLowerCase();
        return queryTables().stream().anyMatch(lowercaseTableName::equals);
    }

    public List<String> queryTables() {
        return query("SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA <> 'INFORMATION_SCHEMA'").stream()
                .map(rec -> rec.get("table_name").toLowerCase())
                .collect(Collectors.toList());
    }

    public List<Map<String, String>> query(final String sql) {
        List<Map<String, String>> l = new ArrayList<>();

        try (Statement stat = this.mainConnection.createStatement(); ResultSet rc = stat.executeQuery(sql)) {
            final ResultSetMetaData m = rc.getMetaData();
            while (rc.next()) {
                Map<String, String> record = new LinkedHashMap<>();
                for (int i = 1; i <= m.getColumnCount(); i++) {
                    record.put(m.getColumnName(i).toLowerCase(), rc.getString(i));
                }
                l.add(record);
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }

        return l;
    }

    public int update(String sql) {
        try (Statement stat = this.mainConnection.createStatement()) {
            return stat.executeUpdate(sql);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }


    private static MemoryDatabase createUnvalidated(String dbName) {
        try {
            final String connectionUrl = "jdbc:h2:mem:" + dbName;
            final Connection con = createConnection(connectionUrl);
            return new MemoryDatabase(connectionUrl, con);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private boolean hasSensingTable() {
        return queryTables().stream().anyMatch(SENSING_TABLE::equals);
    }

    private void createSensingTable() {
        final String sql = "CREATE TABLE " + SENSING_TABLE + " ( DUMMY VARCHAR(100), PRIMARY KEY (DUMMY))";
        try (PreparedStatement stat = this.mainConnection.prepareStatement(sql)) {
            stat.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Connection createConnection(final String connectionUrl) throws SQLException {
        return DriverManager.getConnection(connectionUrl, "sa", "");
    }

}
