package liquibase.database;

import liquibase.changelog.ChangeLogHistoryService;
import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.changelog.OfflineChangeLogHistoryService;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.logging.LogService;
import liquibase.logging.LogType;
import liquibase.parser.SnapshotParser;
import liquibase.parser.SnapshotParserFactory;
import liquibase.resource.ResourceAccessor;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.EmptyDatabaseSnapshot;
import liquibase.snapshot.InvalidExampleException;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Catalog;
import liquibase.structure.core.Schema;
import liquibase.util.ObjectUtil;
import liquibase.util.StringUtils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OfflineConnection implements DatabaseConnection {
    private final String url;
    private final String databaseShortName;
    private final Map<String, String> databaseParams = new HashMap<>();
    private DatabaseSnapshot snapshot;
    private OutputLiquibaseSql outputLiquibaseSql = OutputLiquibaseSql.NONE;
    private String changeLogFile = "databasechangelog.csv";
    private boolean caseSensitive;
    private String productName;
    private String productVersion;
    private int databaseMajorVersion = 999;
    private int databaseMinorVersion = 999;
    private String catalog;
    private boolean sendsStringParametersAsUnicode = true;
    private String connectionUserName;

    public OfflineConnection(String url, ResourceAccessor resourceAccessor) {
        this.url = url;
        Matcher matcher = Pattern.compile("offline:(\\w+)\\??(.*)").matcher(url);
        if (!matcher.matches()) {
            throw new UnexpectedLiquibaseException("Could not parse offline url " + url);
        }
        this.databaseShortName = matcher.group(1).toLowerCase();
        String params = StringUtils.trimToNull(matcher.group(2));
        try {
            Map<String, String> params1 = new HashMap<String, String>();
            if (params != null) {
                String[] keyValues = params.split("&");
                for (String param : keyValues) {
                    String[] split = param.split("=");
                    params1.put(URLDecoder.decode(split[0], "UTF-8"), URLDecoder.decode(split[1], "UTF-8"));
                }
            }


            this.productName = "Offline " + databaseShortName;
            for (Map.Entry<String, String> paramEntry : params1.entrySet()) {

                if ("version".equals(paramEntry.getKey())) {
                    this.productVersion = paramEntry.getValue();
                    String[] versionParts = productVersion.split("\\.");
                    try {
                        this.databaseMajorVersion = Integer.parseInt(versionParts[0]);
                        if (versionParts.length > 1) {
                            this.databaseMinorVersion = Integer.parseInt(versionParts[1]);
                        }
                    } catch (NumberFormatException e) {
                        LogService.getLog(getClass()).warning(LogType.LOG, "Cannot parse database version " + productVersion);
                    }
                } else if ("productName".equals(paramEntry.getKey())) {
                    this.productName = paramEntry.getValue();
                } else if ("catalog".equals(paramEntry.getKey())) {
                    this.catalog = params1.get("catalog");
                } else if ("caseSensitive".equals(paramEntry.getKey())) {
                    this.caseSensitive = Boolean.parseBoolean(paramEntry.getValue());
                } else if ("changeLogFile".equals(paramEntry.getKey())) {
                    this.changeLogFile = paramEntry.getValue();
                } else if ("outputLiquibaseSql".equals(paramEntry.getKey())) {
                    this.outputLiquibaseSql = OutputLiquibaseSql.fromString(paramEntry.getValue());
                } else if ("snapshot".equals(paramEntry.getKey())) {
                    String snapshotFile = paramEntry.getValue();
                    try {
                        SnapshotParser parser = SnapshotParserFactory.getInstance()
                                .getParser(snapshotFile, resourceAccessor);
                        this.snapshot = parser.parse(snapshotFile, resourceAccessor);
                        this.productVersion = this.snapshot.getDatabase().getDatabaseProductVersion();
                        this.snapshot.getDatabase().setConnection(this);

                        for (Catalog catalog : this.snapshot.get(Catalog.class)) {
                            if (catalog.isDefault()) {
                                this.catalog = catalog.getName();
                            }
                        }
                    } catch (LiquibaseException e) {
                        throw new UnexpectedLiquibaseException("Cannot parse snapshot " + url, e);
                    }
                } else if ("sendsStringParametersAsUnicode".equals(paramEntry.getKey())) {
                    this.sendsStringParametersAsUnicode = Boolean.parseBoolean(paramEntry.getValue());
                } else {
                    this.databaseParams.put(paramEntry.getKey(), paramEntry.getValue());
                }
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
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
            } catch (Exception e) {
                LogService.getLog(getClass()).warning(LogType.LOG, "Error setting database parameter " + param.getKey() + ": " + e.getMessage(), e);
            }
        }
        if (database instanceof AbstractJdbcDatabase) {
            ((AbstractJdbcDatabase) database).setCaseSensitive(this.caseSensitive);
        }

        if (snapshot == null) {
            try {
                snapshot = new EmptyDatabaseSnapshot(database);
            } catch (DatabaseException | InvalidExampleException e) {
                throw new UnexpectedLiquibaseException(e);
            }
        }

        ChangeLogHistoryServiceFactory.getInstance().register(createChangeLogHistoryService(database));
    }

    protected ChangeLogHistoryService createChangeLogHistoryService(Database database) {
        return new OfflineChangeLogHistoryService(database, new File(changeLogFile),
                outputLiquibaseSql != OutputLiquibaseSql.NONE, // Output DML
                outputLiquibaseSql == OutputLiquibaseSql.ALL   // Output DDL
        );
    }

    /**
     * Returns a copy of the current simulated content  of the database, filtered by the given
     * array.
     *
     * @param examples the list of objects to clone
     * @return a new DatabaseSnapshot object containing all objects matching examples. If none are found,
     * an empty DatabaseSnapshot is returned.
     */
    public DatabaseSnapshot getSnapshot(DatabaseObject[] examples) {
        return this.snapshot.clone(examples);
    }

    /**
     * For debugging purposes: sets a DatabaseSnapshot object for this connection. Effectively,
     * this simulates the content of the database in this OfflineConnection.
     *
     * @param snapshot the snapshot with the simulated database content
     */
    public void setSnapshot(DatabaseSnapshot snapshot) {
        this.snapshot = snapshot;
        this.snapshot.getDatabase().setConnection(this);

        for (Catalog catalog : this.snapshot.get(Catalog.class)) {
            if (catalog.isDefault()) {
                this.catalog = catalog.getName();
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
    public void setAutoCommit(boolean autoCommit) throws DatabaseException {

    }

    @Override
    public String getCatalog() throws DatabaseException {
        return catalog;
    }

    public String getSchema() {
        if (snapshot == null) {
            return null;
        }
        for (Schema schema : snapshot.get(Schema.class)) {
            if (schema.isDefault()) {
                return schema.getName();
            }
        }
        return null;
    }

    @Override
    public String nativeSQL(String sql) throws DatabaseException {
        return sql;
    }

    @Override
    public void rollback() throws DatabaseException {

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

    public void setDatabaseMajorVersion(int databaseMajorVersion) {
        this.databaseMajorVersion = databaseMajorVersion;
    }

    public void setProductVersion(String productVersion) {
        this.productVersion = productVersion;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    @Override
    public int getDatabaseMinorVersion() throws DatabaseException {
        return databaseMinorVersion;
    }

    public void setDatabaseMinorVersion(int databaseMinorVersion) {
        this.databaseMinorVersion = databaseMinorVersion;
    }

    @Override
    public String getURL() {
        return url;
    }

    @Override
    public String getConnectionUserName() {
        return connectionUserName;
    }

    public void setConnectionUserName(String connectionUserName) {
        this.connectionUserName = connectionUserName;
    }

    @Override
    public boolean isClosed() throws DatabaseException {
        return false;
    }

    public boolean getSendsStringParametersAsUnicode() {
        return sendsStringParametersAsUnicode;
    }

    public void setSendsStringParametersAsUnicode(boolean sendsStringParametersAsUnicode) {
        this.sendsStringParametersAsUnicode = sendsStringParametersAsUnicode;
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    /**
     * Output Liquibase SQL
     */
    private enum OutputLiquibaseSql {
        /**
         * Don't output anything
         */
        NONE,
        /**
         * Output only INSERT/UPDATE/DELETE
         */
        DATA_ONLY,
        /**
         * Output CREATE TABLE as well
         */
        ALL;

        public static OutputLiquibaseSql fromString(String s) {
            if (s == null) {
                return null;
            }
            s = s.toUpperCase();
            // For backward compatibility true is translated in ALL and false in NONE
            switch (s) {
                case "TRUE":
                    return ALL;
                case "FALSE":
                    return NONE;
                default:
                    return valueOf(s);
            }
        }
    }
}
