package liquibase.database.multipledb;

import liquibase.exception.CommandLineParsingException;
import liquibase.exception.LiquibaseException;
import liquibase.integration.commandline.Main;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;

public class MultipleDatabaseTest {

    private static Logger log = Logger.getLogger(MultipleDatabaseTest.class.getName());

    private static final String USERNAME_MAIN = "sampl_main";
    private static final String USERNAME_ADMIN = "sampl_admin";
    private static final String USERNAME_LIQUIBASE = "sampl_liquibase";
    //private static final String DB_URL = "jdbc:hsqldb:mem";
    //private static final String DB_URL = "jdbc:hsqldb:mem;database=target";
    private static final String DB_URL = "jdbc:hsqldb:mem:unittest";
    private static final String DB_URL_MAIN = DB_URL;
    private static final String DB_URL_ADMIN = DB_URL;
    private static final String DB_URL_LIQUIBASE = DB_URL;

    private static String fullPathToThisTest = "";

    private Connection connSysAdmin;

    @BeforeClass
    public static void init() {
        log.info("user.dir=" + System.getProperty("user.dir"));
        String pathToThisTest = MultipleDatabaseTest.class.getPackage().getName().replace('.', '/');
        fullPathToThisTest = new File(System.getProperty("user.dir"), "/src/test/java/" + pathToThisTest).getAbsolutePath();
        log.info("fullPathToThisTest=" + fullPathToThisTest);
    }

    @Before
    public void setupHSQLDB() {
        log.finest("begin...");
        connSysAdmin = getConnection(DB_URL, "SA", "");
        executeSql(connSysAdmin, "DROP SCHEMA IF EXISTS " + USERNAME_MAIN + " CASCADE");
        executeSql(connSysAdmin, "DROP SCHEMA IF EXISTS " + USERNAME_ADMIN + " CASCADE");
        executeSql(connSysAdmin, "DROP SCHEMA IF EXISTS " + USERNAME_LIQUIBASE + " CASCADE");
        executeSql(connSysAdmin, "DROP USER \"" + USERNAME_MAIN + "\"", java.sql.SQLInvalidAuthorizationSpecException.class);
        executeSql(connSysAdmin, "DROP USER \"" + USERNAME_ADMIN + "\"", java.sql.SQLInvalidAuthorizationSpecException.class);
        executeSql(connSysAdmin, "DROP USER \"" + USERNAME_LIQUIBASE + "\"", java.sql.SQLInvalidAuthorizationSpecException.class);
        executeSql(connSysAdmin, "CREATE USER \"" + USERNAME_MAIN + "\" PASSWORD 'qwe'");
        executeSql(connSysAdmin, "CREATE USER \"" + USERNAME_ADMIN + "\" PASSWORD 'qwe'");
        executeSql(connSysAdmin, "CREATE SCHEMA " + USERNAME_MAIN + " AUTHORIZATION \"" + USERNAME_MAIN + "\"");
        executeSql(connSysAdmin, "CREATE SCHEMA " + USERNAME_ADMIN + " AUTHORIZATION \"" + USERNAME_ADMIN + "\"");
        executeSql(connSysAdmin, "ALTER USER \"" + USERNAME_MAIN + "\" SET INITIAL SCHEMA " + USERNAME_MAIN);
        executeSql(connSysAdmin, "ALTER USER \"" + USERNAME_ADMIN + "\" SET INITIAL SCHEMA " + USERNAME_ADMIN);

        assertTable(connSysAdmin, "INFORMATION_SCHEMA.SYSTEM_USERS", 3);
        log.finest("end.");
    }

    @After
    public void teardown() {
        log.finest("begin...");
        try {
            connSysAdmin.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        log.finest("end.");
    }

    @Test
    public void testNativeHSQLDB_admin() {
        log.finest("begin...");
        Connection conn = getConnection(DB_URL, USERNAME_ADMIN, "qwe");
        String sql;
        sql = "CREATE TABLE sampl_admin.users (\n" +
                "    ID INT PRIMARY KEY,\n" +
                "    LOGIN VARCHAR(32) NOT NULL,\n" +
                "    PSWDCRYD VARCHAR(128) NOT NULL,\n" +
                "    PERSON_ID INT NOT NULL\n" +
                ")";
        executeSql(conn, sql);

        assertTable(conn, "sampl_admin.users", 0);
        assertTable(conn, "users", 0);

        assertExecuteSql(conn, "INSERT INTO users (ID, LOGIN, PSWDCRYD, PERSON_ID) VALUES (1, 'nvoxland', 'AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA', 1);\n");

        assertTable(conn, "sampl_admin.users", 1);
        assertTable(conn, "users", 1);

        try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        assertTable(connSysAdmin, "sampl_admin.users", 1);
        log.finest("end.");
    }

    @Test
    public void testNativeHSQLDB_liquibase() {
        log.finest("begin...");

        // ADMIN!!!
        executeSql(connSysAdmin, "CREATE USER \"" + USERNAME_LIQUIBASE + "\" PASSWORD 'qwe' ADMIN");
        executeSql(connSysAdmin, "CREATE SCHEMA " + USERNAME_LIQUIBASE + " AUTHORIZATION \"" + USERNAME_LIQUIBASE + "\"");
        executeSql(connSysAdmin, "ALTER USER \"" + USERNAME_LIQUIBASE + "\" SET INITIAL SCHEMA " + USERNAME_LIQUIBASE);

        Connection conn = getConnection(DB_URL, USERNAME_LIQUIBASE, "qwe");
        String sql;
        sql = "CREATE TABLE sampl_admin.users (\n" +
                "    ID INT PRIMARY KEY,\n" +
                "    LOGIN VARCHAR(32) NOT NULL,\n" +
                "    PSWDCRYD VARCHAR(128) NOT NULL,\n" +
                "    PERSON_ID INT NOT NULL\n" +
                ")";
        executeSql(conn, sql);

        assertTable(conn, "sampl_admin.users", 0);
        assertNoTable(conn, "users");

        sql = "CREATE TABLE DATABASECHANGELOG (\n" +
                "    ID INT PRIMARY KEY,\n" +
                ")";
        executeSql(conn, sql);
        assertTable(conn, USERNAME_LIQUIBASE + ".DATABASECHANGELOG", 0);
        executeSql(conn, "DROP TABLE DATABASECHANGELOG");
        assertNoTable(conn, USERNAME_LIQUIBASE + ".DATABASECHANGELOG");

        try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        assertTable(connSysAdmin, "sampl_admin.users", 0);
        log.finest("end.");
    }

    @Test
    public void testRunAsLiquibaseUser() {
        runLiquibaseTest(true, "versionX/changelog-cumulative.xml");
    }

    @Test
    public void testRunByMultipleDbConnections() {
        runLiquibaseTest(false, "versionX/changelog-multipledb-cumulative.xml");
    }

    @Test
    public void testRunAllByMultipleDbConnections() {
        runLiquibaseTest(false, "changelog-multipledb-cumulative.xml");
    }

    private void runLiquibaseTest(boolean runAsLiquibaseUserOnly, String changeLogFilePath) {
        log.finest("begin...");

        if (runAsLiquibaseUserOnly) {
            // ADMIN!!!
            executeSql(connSysAdmin, "CREATE USER \"" + USERNAME_LIQUIBASE + "\" PASSWORD 'qwe' ADMIN");
        } else {
            // No ADMIN!!!
            executeSql(connSysAdmin, "CREATE USER \"" + USERNAME_LIQUIBASE + "\" PASSWORD 'qwe'");
        }
        executeSql(connSysAdmin, "CREATE SCHEMA " + USERNAME_LIQUIBASE + " AUTHORIZATION \"" + USERNAME_LIQUIBASE + "\"");
        executeSql(connSysAdmin, "ALTER USER \"" + USERNAME_LIQUIBASE + "\" SET INITIAL SCHEMA " + USERNAME_LIQUIBASE);

        String args[] = new String[] {
                "--url=" + DB_URL_LIQUIBASE,
                "--username=" + USERNAME_LIQUIBASE,
                "--password=qwe",
                "--changeLogFile=" + new File(fullPathToThisTest, changeLogFilePath).getAbsolutePath(),
                "--liquibaseSchemaName=" + USERNAME_LIQUIBASE, // Need for HyperSQL: default - "PUBLIC", TODO: 01.03.2018 Release method HsqlDatabase.getConnectionSchemaNameCallStatement()
                "--logLevel=INFO",
                "update"
        };
        if (!runAsLiquibaseUserOnly) {
            String addArgs[] = new String[] {
                    "-Dsampl_main.url=" + DB_URL_MAIN,
                    "-Dsampl_main.username=" + USERNAME_MAIN,
                    "-Dsampl_main.password=qwe",
                    //"-Dsampl_admin.url=" + DB_URL_ADMIN, // Same URL does not requered to specify
                    "-Dsampl_admin.username=" + USERNAME_ADMIN,
                    "-Dsampl_admin.password=qwe"
            };
            int argsLength = args.length;
            int addArgsLength = addArgs.length;
            args = Arrays.copyOf(args, argsLength + addArgsLength);
            System.arraycopy(addArgs, 0, args, argsLength, addArgsLength);
        }
        log.fine("liquibase args=" + Arrays.toString(args));

        try {
            Main.run(args);
        } catch (CommandLineParsingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (LiquibaseException e) {
            throw new RuntimeException(e);
        }

        assertTable(connSysAdmin, USERNAME_MAIN +".persons", 3);
        assertTable(connSysAdmin, USERNAME_ADMIN + ".users", 2);
        assertTable(connSysAdmin, USERNAME_LIQUIBASE + ".DATABASECHANGELOG", 3);

        Connection conn = getConnection(DB_URL, USERNAME_MAIN, "qwe");
        try {
            assertTable(conn, USERNAME_MAIN +".persons", 3);
            assertTable(conn, USERNAME_MAIN +".v_persons", 3);
            assertTable(conn, USERNAME_ADMIN + ".users", 2);
            assertNoTable(conn, USERNAME_ADMIN + ".v_users");
            assertNoTable(conn, USERNAME_LIQUIBASE + ".DATABASECHANGELOG");

            // This does not work for Oracle!!! (if run as Liquibase user only)
            assertExecuteSql(conn, "DELETE FROM sampl_main.persons WHERE ID = 2");

            assertNotExecuteSql(conn, "DELETE FROM sampl_admin.users WHERE ID = 2");
        } finally {
            try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }

        conn = getConnection(DB_URL, USERNAME_ADMIN, "qwe");
        try {
            assertTable(conn, USERNAME_MAIN +".persons", 2);
            assertTable(conn, USERNAME_MAIN +".v_persons", 2);
            assertTable(conn, USERNAME_ADMIN + ".users", 2);
            assertTable(conn, USERNAME_ADMIN + ".v_users", 1);
            assertNoTable(conn, USERNAME_LIQUIBASE + ".DATABASECHANGELOG");

            // This does not work for Oracle!!! (if run as Liquibase user only)
            assertExecuteSql(conn, "DELETE FROM sampl_admin.users WHERE ID = 2");

            assertNotExecuteSql(conn, "DELETE FROM sampl_main.persons WHERE ID = 3");
        } finally {
            try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }

        log.finest("end.");
    }

    private Connection getConnection(String dbUrl, String userName, String password) {
        Connection conn;
        Properties connectionProps = new Properties();
        if (userName != null) connectionProps.put("user", userName);
        if (password != null) connectionProps.put("password", password);

        try {
            conn = DriverManager.getConnection(dbUrl, connectionProps);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
        return conn;
    }

    private int executeSql(Connection conn, String sql) {
        return executeSql(conn, sql, null);
    }

    private <T extends Exception> int executeSql(Connection conn, String sql, Class<T> ignoredExceptionClass) {
        CallableStatement stmt = null;
        try {
            stmt = conn.prepareCall(sql);
            int result = stmt.executeUpdate();
            return result;
        } catch (SQLException ex) {
            if (ignoredExceptionClass == null || !ignoredExceptionClass.isAssignableFrom(ex.getClass())) {
                throw new RuntimeException(ex);
            }
        } finally {
            if (stmt != null) { try {stmt.close();} catch (SQLException e) {e.printStackTrace();}}
        }
        return 0;
    }

    private void assertExecuteSql(Connection conn, String sql) {
        CallableStatement stmt = null;
        try {
            stmt = conn.prepareCall(sql);
            int result = stmt.executeUpdate();
        } catch (SQLException ex) {
            if (isTableNotFoundException("s", ex)) {
                throw new AssertionError("Insufficient rights to execute " + sql);
            }
            throw new RuntimeException(ex);
        } finally {
            if (stmt != null) { try {stmt.close();} catch (SQLException e) {e.printStackTrace();}}
        }
    }

    private void assertNotExecuteSql(Connection conn, String sql) {
        CallableStatement stmt = null;
        try {
            stmt = conn.prepareCall(sql);
            int result = stmt.executeUpdate();
            throw new AssertionError("There should be no rights to execute " + sql);
        } catch (SQLException ex) {
            if (!isTableNotFoundException("s", ex)) {
                throw new RuntimeException(ex);
            }
        } finally {
            if (stmt != null) { try {stmt.close();} catch (SQLException e) {e.printStackTrace();}}
        }
    }

    private void assertTable(Connection conn, String tableName, int rowCount) {
        Statement stmt = null;
        ResultSet rs = null;

        try {
            String sql;
            sql = "select CURRENT_TIMESTAMP, t.* from " + tableName + " t";
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            int i = 0;
            ResultSetMetaData rsMetaData = rs.getMetaData();
            int columnCount = rsMetaData.getColumnCount();
            if (rs.next()) {
                if (log.isLoggable(Level.FINE)) {
                    StringBuilder sb = new StringBuilder(1000);
                    for (int j = 1; j <= columnCount; j++) {
                        if (j > 1) {
                            sb.append(", ");
                        }
                        int columnType = rsMetaData.getColumnType(j);
                        sb.append(rsMetaData.getColumnName(j));
                        if (!(columnType > 0 && columnType < 1000)) {
                            sb.append('(');
                            sb.append(columnType);
                            sb.append(')');
                        }
                    }
                    // default java.util.logging.ConsoleHandler.level = INFO
                    log.info(tableName);
                    log.info(sb.toString());
                }
                do {
                    if (log.isLoggable(Level.FINE)) {
                        StringBuilder sb = new StringBuilder(1000);
                        for (int j = 1; j <= columnCount; j++) {
                            if (j > 1) {
                                sb.append(", ");
                            }
                            int columnType = rsMetaData.getColumnType(j);
                            if (columnType > 0 && columnType < 1000) {
                                sb.append(rs.getString(j));
                            } else {
                                sb.append('*');
                            }
                        }
                        log.info(i + ": " + sb);
                    }
                    i++;
                } while (rs.next());
                if (log.isLoggable(Level.FINE)) log.info(tableName + ": rows=" + i);
            } else {
                if (log.isLoggable(Level.FINE)) log.info(tableName + " is empty");
            }
            if (rowCount >= 0) {
                assertEquals(i, rowCount);
            }
        } catch (SQLException ex) {
            if (isTableNotFoundException(tableName, ex)) {
                throw new AssertionError("Table " + tableName + " not found.");
            }
            throw new RuntimeException(ex);
        } finally {
            if (rs != null) { try {rs.close();} catch (SQLException e) {e.printStackTrace();}}
            if (stmt != null) { try {stmt.close();} catch (SQLException e) {e.printStackTrace();}}
        }
    }

    private void assertNoTable(Connection conn, String tableName) {
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery("select * from " + tableName);
            throw new AssertionError("Table " + tableName + " exists!");
        } catch (SQLException ex) {
            if (!isTableNotFoundException(tableName, ex)) {
                throw new RuntimeException(ex);
            }
        } finally {
            if (rs != null) { try {rs.close();} catch (SQLException e) {e.printStackTrace();}}
            if (stmt != null) { try {stmt.close();} catch (SQLException e) {e.printStackTrace();}}
        }
    }

    private boolean isTableNotFoundException(String tableName, SQLException ex) {
        if (ex instanceof SQLSyntaxErrorException) {
            String message = ex.getMessage();
            if (message != null && message.startsWith("user lacks privilege or object not found: ")) {
                int dotIndex = tableName.lastIndexOf('.');
                return message.endsWith(tableName.substring(dotIndex + 1).toUpperCase());
            }
        }
        return false;
    }

}
