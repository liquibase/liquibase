package liquibase.database;

import liquibase.JUnitScope;
import liquibase.Scope;
import liquibase.database.core.UnsupportedDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.sdk.TemplateService;
import liquibase.structure.DatabaseObject;
import liquibase.structure.ObjectName;
import liquibase.util.CollectionUtil;
import testmd.logic.SetupResult;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.*;

public abstract class ConnectionSupplier implements Cloneable {

    public static final String CONFIG_NAME_STANDARD = "standard";
    public static final String OS_LINUX = "linux";
    public static final String OS_WINDOWS = "windows";

    public String VAGRANT_BOX_NAME_WINDOWS_STANDARD = "liquibase.windows.2008r2.x64";
    public String VAGRANT_BOX_NAME_LINUX_STANDARD = "liquibase.linux.centos.x64";

    private String version;
    private String ipAddress = "10.10.100.100";
    private String os = OS_LINUX;
    private DatabaseConnection connection;
    private SetupResult connectionResult;

    public abstract String getDatabaseShortName();

    public String getConfigurationName() {
        return CONFIG_NAME_STANDARD;
    }

    public abstract String getJdbcUrl();

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getPrimaryCatalog() {
        return "LBCAT";
    }

    public String getPrimarySchema() {
        return "LBSCHEMA";
    }

    public String getDatabaseUsername() {
        return "lbuser";
    }

    public String getDatabasePassword() {
        return "lbuser";
    }

    public String getAlternateUsername() {
        return "lbuser2";
    }

    public String getAlternateUserPassword() {
        return "lbuser2";
    }

    public String getAlternateCatalog() {
        return "LBCAT2";
    }

    public String getAlternateSchema() {
        return "LBSCHEMA2";
    }

    public String getAlternateTablespace() {
        return "lbtbsp2";
    }

    public String getAdminUsername() {
        return "lbadmin";
    }

    ;

    public String getAdminPassword() {
        return "lbadmin";
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Set<String> getPuppetModules() {
        return new HashSet<String>();
    }

    public Set<String> getPuppetForges(String boxName) {
        HashSet<String> forges = new HashSet<String>();
        forges.add("http://forge.puppetlabs.com");

        return forges;
    }

    public String getVagrantBaseBoxName() {
        if (getOs().equals(OS_WINDOWS)) {
            return VAGRANT_BOX_NAME_WINDOWS_STANDARD;
        }
        return VAGRANT_BOX_NAME_LINUX_STANDARD;
    }

    public Set<String> getRequiredPackages(String vagrantBoxName) {
        return new HashSet<String>();
    }

//    public abstract ConfigTemplate getPuppetTemplate(Map<String, Object> context);

    public String getVersion() {
        return version;
    }

    public String getShortVersion() {
        if (getVersion() == null) {
            return "LATEST";
        }
        String[] split = getVersion().split("\\.");
        if (split.length == 1) {
            return split[0];
        } else {
            return split[0] + "." + split[1];
        }
    }


    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return getDatabaseShortName() + "[config:" + getConfigurationName() + "]";
    }

    public String getDescription() {
        String version = getVersion();
        if (version == null) {
            version = "LATEST";
        }

        return "JDBC Url: " + getJdbcUrl() + "\n" +
                "Version: " + version + "\n" +
                "Standard User: " + getDatabaseUsername() + "\n" +
                "         Password: " + getDatabasePassword() + "\n" +
                "Primary Catalog: " + getPrimaryCatalog() + "\n" +
                "Primary Schema: " + getPrimarySchema() + " (if applicable)\n" +
                "\n" +
                "Alternate User: " + getAlternateUsername() + "\n" +
                "          Password: " + getAlternateUserPassword() + "\n" +
                "Alternate Catalog: " + getAlternateCatalog() + "\n" +
                "Alternate Schema: " + getAlternateSchema() + " (if applicable)\n" +
                "Alternate Tablespace: " + getAlternateTablespace() + "\n";
    }

    public Set<ConfigTemplate> generateConfigFiles(Map<String, Object> context) throws IOException {
        Set<ConfigTemplate> set = new HashSet<ConfigTemplate>();
        return set;
    }

    protected boolean isWindows() {
        return getOs().equalsIgnoreCase(OS_WINDOWS);
    }

    protected boolean isLinux() {
        return getOs().equalsIgnoreCase(OS_LINUX);
    }

    public String getFileSeparator() {
        if (isWindows()) {
            return "\\";
        } else {
            return "/";
        }
    }

    protected DatabaseConnection getConnection() throws SetupResult {
        if (connection == null && connectionResult == null) {
            try {
                Connection dbConn = DriverManager.getConnection(this.getJdbcUrl(), this.getDatabaseUsername(), this.getDatabasePassword());
                connection = new JdbcConnection(dbConn);

                Database initDb = JUnitScope.getInstance().getSingleton(DatabaseFactory.class).findCorrectDatabaseImplementation(connection);
                initDb.setConnection(connection);
                initConnection(JUnitScope.getInstance(initDb));
            } catch (Exception e) {
                connectionResult = new SetupResult.CannotVerify("Cannot open connection: " + e.getMessage());
            }
        }
        if (connection != null) {
            return connection;
        } else {
            throw connectionResult;
        }
    }

    protected void initConnection(Scope scope) {

    }

    public Database getDatabase() {
        return DatabaseFactory.getInstance().getDatabase(getDatabaseShortName());
    }

    public Scope connect(Scope scope) throws DatabaseException {
        DatabaseConnection databaseConnection = getConnection();
        Database db = scope.get(Scope.Attr.database, Database.class);
        if (!(db instanceof UnsupportedDatabase) && !db.isCorrectDatabaseImplementation(databaseConnection)) {
            throw new DatabaseException("Incorrect db '" + db.getShortName() + "' for connection " + databaseConnection.getURL());
        }
        db.setConnection(databaseConnection);

        return scope;
    }

    public Database openDatabase() throws DatabaseException {
        DatabaseConnection databaseConnection = getConnection();
        Database db = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(databaseConnection);
        db.setConnection(databaseConnection);

        return db;
    }

    public List<ObjectName> getReferenceObjectNames(Class<? extends DatabaseObject> type, boolean includePartials, boolean includeNulls) {
        return getObjectNames(type, getDatabase().getMaxReferenceContainerDepth(), includePartials, includeNulls);
    }

    public List<ObjectName> getSnapshotObjectNames(Class<? extends DatabaseObject> type, boolean includePartials, boolean includeNulls) {
        return getObjectNames(type, getDatabase().getMaxSnapshotContainerDepth(), includePartials, includeNulls);
    }

    public List<ObjectName> getObjectNames(Class<? extends DatabaseObject> type, int maxDepth, boolean includePartials, boolean includeNulls) {
        List<ObjectName> returnList = new ArrayList<>();

        for (String simpleName : getSimpleObjectNames(type)) {
            for (ObjectName container : getContainers(maxDepth, includePartials, includeNulls)) {
                returnList.add(new ObjectName(simpleName, container));
            }
        }


        return returnList;
    }

    public List<ObjectName> getAllContainers() {
        int maxDepth = getDatabase().getMaxSnapshotContainerDepth();

        if (maxDepth == 0) {
            return Arrays.asList();
        } else if (maxDepth == 1) {
            return Arrays.asList(new ObjectName(getPrimaryCatalog()), new ObjectName(getAlternateCatalog()));
        } else {
            return Arrays.asList(new ObjectName(getPrimaryCatalog(), getPrimarySchema()),
                    new ObjectName(getPrimaryCatalog(), getAlternateSchema()),
                    new ObjectName(getAlternateCatalog(), getPrimarySchema()),
                    new ObjectName(getAlternateCatalog(), getAlternateSchema()));
        }
    }


    protected List<ObjectName> getContainers(int maxDepth, boolean includePartials, boolean includeNulls) {
        LinkedHashSet<ObjectName> returnList = new LinkedHashSet<>();
        for (ObjectName container : getAllContainers()) {
            List<ObjectName> expandedList = new ArrayList<>();

            List<String> name = container.getNameList();
            name = name.subList(name.size() - maxDepth, name.size());
            ObjectName shortened = new ObjectName(name.toArray(new String[name.size()]));
            expandedList.add(shortened);

            if (includePartials) {
                expandedList.addAll(createPartials(expandedList));
            }

            if (includeNulls) {
                expandedList.addAll(createNulls(expandedList));
            }

            returnList.addAll(expandedList);
        }

        return new ArrayList<>(returnList);
    }

    protected List<ObjectName> createPartials(List<ObjectName> originalList) {
        List<ObjectName> returnList = new ArrayList<>();

        for (ObjectName name : originalList) {
            List<String> nameList = name.getNameList();
            for (int i = nameList.size() - 1; i > 0; i--) {
                ObjectName shortened = new ObjectName(nameList.subList(i, nameList.size()).toArray(new String[nameList.size() - i]));
                returnList.add(shortened);

//                if (shortened.getNameList().size() < maxDepth) {
//                    ObjectName nullPadded = new ObjectName(name.subList(i, name.size()).toArray(new String[name.size() - i]));
//                    ObjectName nullContainer = new ObjectName();
//                    nullPadded.set(ObjectName.Attr.container, nullContainer);
//
//                    while (nullPadded.getNameList().size() < maxDepth) {
//                        nullContainer.set(ObjectName.Attr.container, new ObjectName());
//                        nullContainer = nullContainer.getContainer();
//                    }
//                    expandedList.add(nullPadded);
//                }
//
//                expandedList.add(new ObjectName(name.subList(i, name.size()).toArray(new String[name.size() - i])));
            }
        }

        return returnList;
    }

    protected List<ObjectName> createNulls(List<ObjectName> originalList) {
        List<ObjectName> returnList = new ArrayList<>();

        for (ObjectName name : originalList) {
            List<String> nameList = name.getNameList();
            Set<Integer> indexes = new HashSet<>();
            for (int i=0; i<nameList.size(); i++) {
                indexes.add(i);
            }

            for (Collection<Integer> toNull : CollectionUtil.powerSet(indexes)) {
                List<String> modifiedNameList = new ArrayList<>(nameList);
                for (int val : toNull) {
                    modifiedNameList.set(val, null);
                }

                returnList.add(new ObjectName(modifiedNameList.toArray(new String[modifiedNameList.size()])));
            }
        }

        return returnList;
    }

    public List<String> getSimpleObjectNames(Class<? extends DatabaseObject> type) {
        List<String> returnList = new ArrayList<>();
        returnList.add("test_" + type.getSimpleName().toLowerCase());
        returnList.add("TEST_" + type.getSimpleName().toUpperCase());
        returnList.add("Test" + type.getSimpleName());

        return returnList;
    }

    public static class ConfigTemplate {

        private final String templatePath;
        private final Map<String, Object> context;
        private final String outputFileName;

        public ConfigTemplate(String templatePath, Map<String, Object> context) {
            this.templatePath = templatePath;
            this.context = context;
            this.outputFileName = templatePath.replaceFirst(".*/", "").replaceFirst("\\.vm$", "");
        }

        public String getTemplatePath() {
            return templatePath;
        }

        public Map<String, Object> getContext() {
            return context;
        }

        public String getOutputFileName() {
            return outputFileName;
        }

        public void write(File outputFile) throws IOException {
            TemplateService.getInstance().write(templatePath, outputFile, context);
        }

        public String output() throws IOException {
            return TemplateService.getInstance().output(templatePath, context);
        }
    }
}
