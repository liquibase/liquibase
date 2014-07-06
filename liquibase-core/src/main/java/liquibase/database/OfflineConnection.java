package liquibase.database;

import liquibase.changelog.ChangeLogHistoryService;
import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.changelog.OfflineChangeLogHistoryService;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.lockservice.LockService;
import liquibase.lockservice.LockServiceFactory;
import liquibase.logging.LogFactory;
import liquibase.util.ObjectUtil;
import liquibase.util.StringUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OfflineConnection implements DatabaseConnection {
    private final String url;
    private final String databaseShortName;
    private final Map<String, String> params = new HashMap<String, String>();
    private boolean outputLiquibaseSql = false;
    private String changeLogFile = "databasechangelog.csv";
    private Boolean caseSensitive = false;
    private String productName;
    private String productVersion;
    private int databaseMajorVersion = 999;
    private int databaseMinorVersion = 999;
    private String catalog;

    private final Map<String, String> databaseParams = new HashMap<String, String>();

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


        this.productName = "Offline "+databaseShortName;
        for (Map.Entry<String, String> paramEntry : this.params.entrySet()) {

            if (paramEntry.getKey().equals("version")) {
                this.productVersion = paramEntry.getValue();
                String[] versionParts = productVersion.split("\\.");
                try {
                    this.databaseMajorVersion = Integer.valueOf(versionParts[0]);
                    if (versionParts.length > 1) {
                        this.databaseMinorVersion = Integer.valueOf(versionParts[1]);
                    }
                } catch (NumberFormatException e) {
                    LogFactory.getInstance().getLog().warning("Cannot parse database version "+productVersion);
                }
            } else if (paramEntry.getKey().equals("productName")) {
                this.productName = paramEntry.getValue();
            } else if (paramEntry.getKey().equals("catalog")) {
                this.catalog = this.params.get("catalog");
            } else if (paramEntry.getKey().equals("caseSensitive")) {
                 this.caseSensitive = Boolean.valueOf(paramEntry.getValue());
            } else if (paramEntry.getKey().equals("changeLogFile")) {
                this.changeLogFile = paramEntry.getValue();
            } else if (paramEntry.getKey().equals("outputLiquibaseSql")) {
                this.outputLiquibaseSql = Boolean.valueOf(paramEntry.getValue());
            } else {
                this.databaseParams.put(paramEntry.getKey(), paramEntry.getValue());
            }
        }
    }

    public boolean isCorrectDatabaseImplementation(Database database) {
        return database.getShortName().equalsIgnoreCase(databaseShortName);
    }

    @Override
    public void attached(Database database) {
        for (Map.Entry<String, String> param : this.databaseParams.entrySet()) {
            try {
                ObjectUtil.setProperty(database, param.getKey(), param.getValue());
            } catch (Throwable e) {
                LogFactory.getInstance().getLog().warning("Error setting database parameter " + param.getKey() + ": " + e.getMessage(), e);
            }
        }
        if (database instanceof AbstractJdbcDatabase) {
            ((AbstractJdbcDatabase) database).setCaseSensitive(this.caseSensitive);
        }

        ChangeLogHistoryServiceFactory.getInstance().register(createChangeLogHistoryService(database));
    }

    protected ChangeLogHistoryService createChangeLogHistoryService(Database database) {
        return new OfflineChangeLogHistoryService(database, new File(changeLogFile), outputLiquibaseSql);
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
        return catalog;
    }

    @Override
    public String nativeSQL(String sql) throws DatabaseException {
        return sql;
    }

    @Override
    public void rollback() throws DatabaseException {

    }

    @Override
    public void setAutoCommit(boolean autoCommit) throws DatabaseException {

    }

    @Override
    public String getDatabaseProductName() throws DatabaseException {
        return productName;
    }

    @Override
    public String getDatabaseProductVersion() throws DatabaseException {
        return productVersion;
    }

    @Override
    public int getDatabaseMajorVersion() throws DatabaseException {
        return databaseMajorVersion;
    }

    @Override
    public int getDatabaseMinorVersion() throws DatabaseException {
        return databaseMinorVersion;
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

    public boolean getOutputLiquibaseSql() {
        return outputLiquibaseSql;
    }

    public void setOutputLiquibaseSql(Boolean outputLiquibaseSql) {
        this.outputLiquibaseSql = outputLiquibaseSql;
    }
}
