package liquibase.sdk.supplier.database;

import liquibase.database.Database;
import liquibase.servicelocator.ServiceLocator;

import java.util.*;

public class ConnectionConfigurationFactory {

    private static ConnectionConfigurationFactory instance;

    private Map<String, Set<ConnectionConfiguration>> configsByDatabase = new HashMap<String, Set<ConnectionConfiguration>>();

    public ConnectionConfigurationFactory() {
        try {
            Class[] classes = ServiceLocator.getInstance().findClasses(ConnectionConfiguration.class);

            //noinspection unchecked
            for (Class<? extends ConnectionConfiguration> clazz : classes) {
                register(clazz.getConstructor().newInstance());
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public static ConnectionConfigurationFactory getInstance() {
        if (instance == null) {
            instance = new ConnectionConfigurationFactory();
        }
        return instance;
    }

    public Set<ConnectionConfiguration> getConfigurations(Database database) {
        Set<ConnectionConfiguration> configurations = configsByDatabase.get(database.getShortName());
        if (configurations == null) {
            return new HashSet<ConnectionConfiguration>();
        }
        return configurations;
    }

    public static void reset() {
        instance = new ConnectionConfigurationFactory();
    }

    public void register(ConnectionConfiguration config) {
        String databaseShortName = config.getDatabaseShortName();
        if (!configsByDatabase.containsKey(databaseShortName)) {
            configsByDatabase.put(databaseShortName, new HashSet<ConnectionConfiguration>());
        }
        configsByDatabase.get(databaseShortName).add(config);
    }

    public Collection<ConnectionConfiguration> findConfigurations(List<String> descriptions) throws UnknownDatabaseException {
        List<ConnectionConfiguration> returnList = new ArrayList<ConnectionConfiguration>();
        for (String config : descriptions) {
            Map<String, String> params = parseConfig(config);
            if (!params.containsKey("config")) {
                params.put("config", "standard");
            }

            String databaseName = params.get("databaseName");
            String configName = params.get("config");

            Set<ConnectionConfiguration> potentialConfigurations = configsByDatabase.get(databaseName);
            if (potentialConfigurations == null) {
                throw new UnknownDatabaseException("No database configurations for "+databaseName);
            }

            boolean foundConfig = false;
            for (ConnectionConfiguration potential : potentialConfigurations) {
                if (potential.getConfigurationName().equals(configName)) {
                    if (params.containsKey("version")) {
                        potential.setVersion(params.get("version"));
                    }
                    if (params.containsKey("hostname")) {
                        potential.setHostname(params.get("hostname"));
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

    private Map<String, String> parseConfig(String config) {
        Map<String, String> params = new HashMap<String, String>();

        String databaseName;
        if (config.contains("[")) {
            databaseName = config.replaceFirst("\\[.*", "").trim();
            String paramString = config.replaceFirst(".*\\[", "").replaceFirst("]$", "");
            for (String keyValue : paramString.split(",")) {
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
