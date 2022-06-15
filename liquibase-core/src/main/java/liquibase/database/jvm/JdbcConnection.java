package liquibase.database.jvm;

import liquibase.Scope;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;

import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A ConnectionWrapper implementation which delegates completely to an
 * underlying java.sql.connection.
 */
public class JdbcConnection implements DatabaseConnection {
    private java.sql.Connection con;
    private static final Set<Map.Entry<Pattern, Pattern>> PATTERN_JDBC_BLANK = new HashSet<>();
    private static final Set<Map.Entry<Pattern, Pattern>> PATTERN_JDBC_BLANK_TO_OBFUSCATE = new HashSet<>();
    private static final Set<Map.Entry<Pattern, Pattern>> PATTERN_JDBC_OBFUSCATE = new HashSet<>();
    private static final Pattern PROXY_USER = Pattern.compile(".*(?:thin|oci)\\:(.+)/@.*");

    @SuppressWarnings("squid:S2068")
    private static final String FILTER_CREDS_PW_TO_BLANK = "(?i)[?&:;]password=[^;&]*";
    private static final String FILTER_CREDS_USER_TO_BLANK = "(?i)[?&:;]user(.*?)=(.+)[^;&]";
    @SuppressWarnings("squid:S2068")
    public static final String FILTER_CREDS_PRIVATE_KEY_TO_BLANK = "(?i)[?&:;]private_key_file(.*?)=[^;&]*";

    @SuppressWarnings("squid:S2068")
    public static final String FILTER_CREDS_PASSWORD = "(?i)(.+?)password=([^;&?]+)[;&]*?(.*?)$";

    public static final String FILTER_CREDS_USER = "(?i)(.+?)user[name]*?=([^;&?]+)[;&]*?(.*?)$";

    @SuppressWarnings("squid:S2068")
    public static final String FILTER_CREDS_PRIVATE_KEY_FILE = "(?i)(.+?)private_key_file=([^;&?]+)[;&]*?(.*?)$";

    @SuppressWarnings("squid:S2068")
    public static final String FILTER_CREDS_PRIVATE_KEY_FILE_PWD = "(?i)(.+?)private_key_file_pwd=([^;&?]+)[;&]*?(.*?)$";

    public static final String FILTER_CREDS = "(?i)/(.*)((?=@))";

    public static final String FILTER_CREDS_MYSQL_TO_OBFUSCATE = "(?i).+://(.*?)([:])(.*?)((?=@))";
    public static final String FILTER_CREDS_ORACLE_TO_OBFUSCATE = "(?i)jdbc:oracle:thin:(.*?)([/])(.*?)((?=@))";

    static {
        PATTERN_JDBC_BLANK.add(PatternPair.of(Pattern.compile("(?i)(.*)"), Pattern.compile(FILTER_CREDS_PW_TO_BLANK)));
        PATTERN_JDBC_BLANK.add(PatternPair.of(Pattern.compile("(?i)(.*)"), Pattern.compile(FILTER_CREDS_USER_TO_BLANK)));
        PATTERN_JDBC_BLANK.add(PatternPair.of(Pattern.compile("(?i)(.*)"), Pattern.compile(FILTER_CREDS_PRIVATE_KEY_TO_BLANK)));
        PATTERN_JDBC_BLANK.add(PatternPair.of(Pattern.compile("(?i)jdbc:oracle:thin(.*)"), Pattern.compile(FILTER_CREDS)));
        PATTERN_JDBC_BLANK.add(PatternPair.of(Pattern.compile("(?i)jdbc:mysql(.*)"), Pattern.compile(FILTER_CREDS)));
        PATTERN_JDBC_BLANK.add(PatternPair.of(Pattern.compile("(?i)jdbc:mariadb(.*)"), Pattern.compile(FILTER_CREDS)));

        PATTERN_JDBC_BLANK_TO_OBFUSCATE.add(PatternPair.of(Pattern.compile("(?i)jdbc:oracle:thin(.*)"), Pattern.compile(FILTER_CREDS_ORACLE_TO_OBFUSCATE)));
        PATTERN_JDBC_BLANK_TO_OBFUSCATE.add(PatternPair.of(Pattern.compile("(?i)jdbc:mysql(.*)"), Pattern.compile(FILTER_CREDS_MYSQL_TO_OBFUSCATE)));
        PATTERN_JDBC_BLANK_TO_OBFUSCATE.add(PatternPair.of(Pattern.compile("(?i)jdbc:mariadb(.*)"), Pattern.compile(FILTER_CREDS_MYSQL_TO_OBFUSCATE)));

        PATTERN_JDBC_OBFUSCATE.add(PatternPair.of(Pattern.compile("(?i)(.*)"), Pattern.compile(FILTER_CREDS_PASSWORD)));
        PATTERN_JDBC_OBFUSCATE.add(PatternPair.of(Pattern.compile("(?i)(.*)"), Pattern.compile(FILTER_CREDS_USER)));
        PATTERN_JDBC_OBFUSCATE.add(PatternPair.of(Pattern.compile("(?i)(.*)"), Pattern.compile(FILTER_CREDS_PRIVATE_KEY_FILE)));
        PATTERN_JDBC_OBFUSCATE.add(PatternPair.of(Pattern.compile("(?i)(.*)"), Pattern.compile(FILTER_CREDS_PRIVATE_KEY_FILE_PWD)));
    }

    public JdbcConnection() {

    }

    public JdbcConnection(java.sql.Connection connection) {
        this.con = connection;
    }

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    @Override
    public void open(String url, Driver driverObject, Properties driverProperties) throws DatabaseException {
        String driverClassName = driverObject.getClass().getName();
        String errorMessage = "Connection could not be created to " + sanitizeUrl(url) + " with driver " + driverClassName;
        try {
            this.con = driverObject.connect(url, driverProperties);
            if (this.con == null) {
                throw new DatabaseException(errorMessage + ".  Possibly the wrong driver for the given database URL");
            }
        } catch (SQLException sqle) {
            if (driverClassName.equals("org.h2.Driver")) {
                errorMessage += ". Make sure your H2 database is active and accessible by opening a new terminal window, run \"liquibase init start-h2\", and then return to this terminal window to run commands";
            }
            throw new DatabaseException(errorMessage + ".  " + sqle.getMessage());
        }
    }

    @Override
    public void attached(Database database) {
        try {
            database.addReservedWords(Arrays.asList(this.getWrappedConnection().getMetaData().getSQLKeywords().toUpperCase().split(",\\s*")));
        } catch (SQLException e) {
            Scope.getCurrentScope().getLog(getClass()).info("Error fetching reserved words list from JDBC driver", e);
        }


    }

    @Override
    public String getDatabaseProductName() throws DatabaseException {
        try {
            return con.getMetaData().getDatabaseProductName();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public String getDatabaseProductVersion() throws DatabaseException {
        try {
            return con.getMetaData().getDatabaseProductVersion();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public int getDatabaseMajorVersion() throws DatabaseException {
        try {
            return con.getMetaData().getDatabaseMajorVersion();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public int getDatabaseMinorVersion() throws DatabaseException {
        try {
            return con.getMetaData().getDatabaseMinorVersion();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public String getURL() {
        try {
            String url = getConnectionUrl();
            url = stripPasswordPropFromJdbcUrl(url);
            return url;
        } catch (SQLException e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    /**
     * Remove any secure information from the URL. Used for logging purposes
     * Strips off the <code>;password=</code> property from string.
     * Note: it does not remove the password from the
     * <code>user:password@host</code> section
     *
     * @param  url string to remove password=xxx from
     * @return modified string
     */
    public static String sanitizeUrl(String url) {
        return obfuscateCredentialsPropFromJdbcUrl(url);
    }

    private static String obfuscateCredentialsPropFromJdbcUrl(String jdbcUrl) {
        if (jdbcUrl == null || (jdbcUrl != null && jdbcUrl.equals(""))) {
            return jdbcUrl;
        }

        //
        // Do not try to strip passwords from a proxy URL
        //
        Matcher m = PROXY_USER.matcher(jdbcUrl);
        if (m.matches()) {
            return jdbcUrl;
        }

        for (Map.Entry<Pattern, Pattern> entry : PATTERN_JDBC_BLANK_TO_OBFUSCATE) {
            Pattern jdbcUrlPattern = entry.getKey();
            Matcher matcher = jdbcUrlPattern.matcher(jdbcUrl);
            if (matcher.matches()) {
                Pattern pattern = entry.getValue();
                Matcher actualMatcher = pattern.matcher(jdbcUrl);
                if (actualMatcher.find()) {
                    jdbcUrl = jdbcUrl.replace(actualMatcher.group(1) + actualMatcher.group(2) + actualMatcher.group(3), "*****" + actualMatcher.group(2) + "*****");
                }
            }
        }

        for (Map.Entry<Pattern, Pattern> entry : PATTERN_JDBC_OBFUSCATE) {
            Pattern jdbcUrlPattern = entry.getKey();
            Matcher matcher = jdbcUrlPattern.matcher(jdbcUrl);
            if (matcher.matches()) {
                Pattern pattern = entry.getValue();
                Matcher actualMatcher = pattern.matcher(jdbcUrl);
                if (actualMatcher.find()) {
                    jdbcUrl = jdbcUrl.replace("=" + actualMatcher.group(2), "=*****");
                }
            }
        }
        return jdbcUrl;
    }

    private static String stripPasswordPropFromJdbcUrl(String jdbcUrl) {
        if (jdbcUrl == null || (jdbcUrl != null && jdbcUrl.equals(""))) {
            return jdbcUrl;
        }

        //
        // Do not try to strip passwords from a proxy URL
        //
        Matcher m = PROXY_USER.matcher(jdbcUrl);
        if (m.matches()) {
            return jdbcUrl;
        }
        for (Map.Entry<Pattern, Pattern> entry : PATTERN_JDBC_BLANK) {
            Pattern jdbcUrlPattern = entry.getKey();
            Matcher matcher = jdbcUrlPattern.matcher(jdbcUrl);
            if (matcher.matches()) {
                Pattern pattern = entry.getValue();
                jdbcUrl = pattern.matcher(jdbcUrl).replaceAll("");
            }
        }
        return jdbcUrl;
    }

    protected String getConnectionUrl() throws SQLException {
        return con.getMetaData().getURL();
    }

    @Override
    public String getConnectionUserName() {
        try {
            return con.getMetaData().getUserName();
        } catch (SQLException e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    /**
     * Returns the connection that this Delegate is using.
     *
     * @return The connection originally passed in the constructor
     */
    public Connection getWrappedConnection() {
        return con;
    }

    public void clearWarnings() throws DatabaseException {
        try {
            con.clearWarnings();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public void close() throws DatabaseException {
        rollback();
        try {
            con.close();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public void commit() throws DatabaseException {
        try {
            if (!con.getAutoCommit()) {
                con.commit();
            }
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public Statement createStatement() throws DatabaseException {
        try {
            return con.createStatement();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public Statement createStatement(int resultSetType,
                                     int resultSetConcurrency, int resultSetHoldability)
            throws DatabaseException {
        try {
            return con.createStatement(resultSetType, resultSetConcurrency,
                    resultSetHoldability);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public Statement createStatement(int resultSetType, int resultSetConcurrency)
            throws DatabaseException {
        try {
            return con.createStatement(resultSetType, resultSetConcurrency);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public boolean getAutoCommit() throws DatabaseException {
        try {
            return con.getAutoCommit();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public String getCatalog() throws DatabaseException {
        try {
            return con.getCatalog();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public int getHoldability() throws DatabaseException {
        try {
            return con.getHoldability();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public DatabaseMetaData getMetaData() throws DatabaseException {
        try {
            return con.getMetaData();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public int getTransactionIsolation() throws DatabaseException {
        try {
            return con.getTransactionIsolation();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public Map<String, Class<?>> getTypeMap() throws DatabaseException {
        try {
            return con.getTypeMap();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public SQLWarning getWarnings() throws DatabaseException {
        try {
            return con.getWarnings();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public boolean isClosed() throws DatabaseException {
        try {
            return con.isClosed();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public boolean isReadOnly() throws DatabaseException {
        try {
            return con.isReadOnly();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public String nativeSQL(String sql) throws DatabaseException {
        try {
            return con.nativeSQL(sql);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public CallableStatement prepareCall(String sql, int resultSetType,
                                         int resultSetConcurrency, int resultSetHoldability)
            throws DatabaseException {
        try {
            return con.prepareCall(sql, resultSetType, resultSetConcurrency,
                    resultSetHoldability);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public CallableStatement prepareCall(String sql, int resultSetType,
                                         int resultSetConcurrency) throws DatabaseException {
        try {
            return con.prepareCall(sql, resultSetType, resultSetConcurrency);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public CallableStatement prepareCall(String sql) throws DatabaseException {
        try {
            return con.prepareCall(sql);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType,
                                              int resultSetConcurrency, int resultSetHoldability)
            throws DatabaseException {
        try {
            return con.prepareStatement(sql, resultSetType, resultSetConcurrency,
                    resultSetHoldability);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType,
                                              int resultSetConcurrency) throws DatabaseException {
        try {
            return con.prepareStatement(sql, resultSetType, resultSetConcurrency);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)
            throws DatabaseException {
        try {
            return con.prepareStatement(sql, autoGeneratedKeys);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public PreparedStatement prepareStatement(String sql, int[] columnIndexes)
            throws DatabaseException {
        try {
            return con.prepareStatement(sql, columnIndexes);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public PreparedStatement prepareStatement(String sql, String[] columnNames)
            throws DatabaseException {
        try {
            return con.prepareStatement(sql, columnNames);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public PreparedStatement prepareStatement(String sql) throws DatabaseException {
        try {
            return con.prepareStatement(sql);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public void releaseSavepoint(Savepoint savepoint) throws DatabaseException {
        try {
            con.releaseSavepoint(savepoint);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public void rollback() throws DatabaseException {
        try {
            if (!con.getAutoCommit() && !con.isClosed()) {
                con.rollback();
            }
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public void rollback(Savepoint savepoint) throws DatabaseException {
        try {
            if (!con.getAutoCommit()) {
                con.rollback(savepoint);
            }
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws DatabaseException {
        // Fix for Sybase jConnect JDBC driver bug.
        // Which throws DatabaseException(JZ016: The AutoCommit option is already set to false)
        // if con.setAutoCommit(false) called twise or more times with value 'false'.
//        if (con.getAutoCommit() != autoCommit) {
        try {
            con.setAutoCommit(autoCommit);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
//        }
    }

    public void setCatalog(String catalog) throws DatabaseException {
        try {
            con.setCatalog(catalog);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public void setHoldability(int holdability) throws DatabaseException {
        try {
            con.setHoldability(holdability);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public void setReadOnly(boolean readOnly) throws DatabaseException {
        try {
            con.setReadOnly(readOnly);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public Savepoint setSavepoint() throws DatabaseException {
        try {
            return con.setSavepoint();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public Savepoint setSavepoint(String name) throws DatabaseException {
        try {
            return con.setSavepoint(name);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public void setTransactionIsolation(int level) throws DatabaseException {
        try {
            con.setTransactionIsolation(level);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public void setTypeMap(Map<String, Class<?>> map) throws DatabaseException {
        try {
            con.setTypeMap(map);
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    public Connection getUnderlyingConnection() {
        return con;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof JdbcConnection)) {
            return false;
        }
        Connection underlyingConnection = this.getUnderlyingConnection();
        if (underlyingConnection == null) {
            return ((JdbcConnection) obj).getUnderlyingConnection() == null;
        }

        return underlyingConnection.equals(((JdbcConnection) obj).getUnderlyingConnection());

    }

    @Override
    public int hashCode() {
        Connection underlyingConnection = this.getUnderlyingConnection();
        try {
            if ((underlyingConnection == null) || underlyingConnection.isClosed()) {
                return super.hashCode();
            }
        } catch (SQLException e) {
            return super.hashCode();
        }
        return underlyingConnection.hashCode();
    }

    public boolean supportsBatchUpdates() throws DatabaseException {
        try {
            return getUnderlyingConnection().getMetaData().supportsBatchUpdates();
        } catch (SQLException e) {
            throw new DatabaseException("Asking the JDBC driver if it supports batched updates has failed.", e);
        }
    }

    private static class PatternPair {
        // Return a map entry (key-value pair) from the specified values
        public static <T, U> Map.Entry<T, U> of(T first, U second) {
            return new AbstractMap.SimpleEntry<>(first, second);
        }
    }
}
