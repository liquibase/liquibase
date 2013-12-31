package liquibase.database;

import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OfflineConnection implements DatabaseConnection {
    private final String url;
    private final String databaseShortName;
    private final Map<String, String> params = new HashMap<String, String>();

    public OfflineConnection(String url) {
        this.url = url;
        Matcher matcher = Pattern.compile("offline:(\\w+)\\??(.*)").matcher(url);
        if (!matcher.matches()) {
            throw new UnexpectedLiquibaseException("Could not parse offline url "+url);
        }
        this.databaseShortName = matcher.group(1).toLowerCase();
        String params = StringUtils.trimToNull(matcher.group(2));
        if (params != null) {
            String[] keyValues = params.split("&");
            for (String param : keyValues) {
                String[] split = param.split("=");
                this.params.put(split[0], split[1]);
            }
        }

    }

    public boolean isCorrectDatabaseImplementation(Database database) {
        return database.getShortName().equalsIgnoreCase(databaseShortName);
    }

    @Override
    public void attached(Database database) {
        if (params.containsKey("caseSensitive")) {
            if (database instanceof AbstractJdbcDatabase) {
                ((AbstractJdbcDatabase) database).setCaseSensitive(Boolean.valueOf(params.get("caseSensitive")));
            }
        }
    }

    @Override
    public void close() throws DatabaseException {
        //nothing
    }

    @Override
    public void commit() throws DatabaseException {
        //nothing
    }

    @Override
    public boolean getAutoCommit() throws DatabaseException {
        return false;
    }

    @Override
    public String getCatalog() throws DatabaseException {
        return null;
    }

    @Override
    public String nativeSQL(String sql) throws DatabaseException {
        return null;
    }

    @Override
    public void rollback() throws DatabaseException {

    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws DatabaseException {

    }

    @Override
    public String getDatabaseProductName() throws DatabaseException {
        return null;
    }

    @Override
    public String getDatabaseProductVersion() throws DatabaseException {
        return null;
    }

    @Override
    public int getDatabaseMajorVersion() throws DatabaseException {
        return 0;
    }

    @Override
    public int getDatabaseMinorVersion() throws DatabaseException {
        return 0;
    }

    @Override
    public String getURL() {
        return url;
    }

    @Override
    public String getConnectionUserName() {
        return null;
    }

    @Override
    public boolean isClosed() throws DatabaseException {
        return false;
    }
}
