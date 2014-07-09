package liquibase.sdk.supplier.database;

import liquibase.database.Database;
import liquibase.servicelocator.ServiceLocator;

import java.util.*;

public class DatabaseInstallerFactory {

    private static DatabaseInstallerFactory instance;

    private Map<String, Set<AbstractDatabaseInstaller>> configsByDatabase = new HashMap<String, Set<AbstractDatabaseInstaller>>();

    public DatabaseInstallerFactory() {
        try {
            Class[] classes = ServiceLocator.getInstance().findClasses(AbstractDatabaseInstaller.class);

            //noinspection unchecked
            for (Class<? extends AbstractDatabaseInstaller> clazz : classes) {
                register(clazz.getConstructor().newInstance());
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public static DatabaseInstallerFactory getInstance() {
        if (instance == null) {
            instance = new DatabaseInstallerFactory();
        }
        return instance;
    }

    public Set<AbstractDatabaseInstaller> getInstallers(Database database) {
        Set<AbstractDatabaseInstaller> configurations = configsByDatabase.get(database.getShortName());
        if (configurations == null) {
            return new HashSet<AbstractDatabaseInstaller>();
        }
        return configurations;
    }

    public static void reset() {
        instance = new DatabaseInstallerFactory();
    }

    public void register(AbstractDatabaseInstaller config) {
        String databaseShortName = config.getDatabaseShortName();
        if (!configsByDatabase.containsKey(databaseShortName)) {
            configsByDatabase.put(databaseShortName, new HashSet<AbstractDatabaseInstaller>());
        }
        configsByDatabase.get(databaseShortName).add(config);
    }

    public Collection<AbstractDatabaseInstaller> findInstallers(String[] descriptions) throws UnknownDatabaseException {
        List<AbstractDatabaseInstaller> returnList = new ArrayList<AbstractDatabaseInstaller>();
        for (String config : descriptions) {
            Map<String, String> params = null;
            params = parseConfig(config);

            if (!params.containsKey("config")) {
                params.put("config", "standard");
            }

            String databaseName = params.get("databaseName");
            String configName = params.get("config");

            Set<AbstractDatabaseInstaller> potentialInstallers = configsByDatabase.get(databaseName);
            if (potentialInstallers == null) {
                throw new UnknownDatabaseException("No database installation configurations for "+databaseName);
            }

            boolean foundConfig = false;
            for (AbstractDatabaseInstaller potential : potentialInstallers) {
                if (potential.getConfigurationName().equals(configName)) {
                    if (params.containsKey("version")) {
                        potential.setVersion(params.get("version"));
                    }
                    if (params.containsKey("hostname")) {
                        potential.setIpAddress(params.get("hostname"));
                    }
                    if (params.containsKey("os")) {
                        potential.setOs(params.get("os"));
                    }
                    returnList.add(potential);
                    foundConfig = true;
                    break;
                }
            }
            if (!foundConfig) {
                throw new UnknownDatabaseException("No database configuration of '"+config+"'");
            }
        }

        return returnList;
    }

    private Map<String, String> parseConfig(String config) throws UnknownDatabaseException {
        Map<String, String> params = new HashMap<String, String>();

        String databaseName;
        if (config.contains("[")) {
            databaseName = config.replaceFirst("\\[.*", "").trim();
            String paramString = config.replaceFirst(".*\\[", "").replaceFirst("]$", "");
            for (String keyValue : paramString.split(",")) {
                if (!keyValue.contains(":")) {
                    throw new UnknownDatabaseException("Error parsing parameter "+keyValue+". Configuration parameters must use colon separated key/value pairs. For example, config:standard");
                }
                String[] split = keyValue.split(":");
                params.put(split[0].trim(), split[1].trim());
            }
        } else {
            databaseName = config;
        }
        params.put("databaseName", databaseName.trim());


        return params;
    }

    public static class UnknownDatabaseException extends Exception {
        public UnknownDatabaseException(String message) {
            super(message);
        }
    }
}
