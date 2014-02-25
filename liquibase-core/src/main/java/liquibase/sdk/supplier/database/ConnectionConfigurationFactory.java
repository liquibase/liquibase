package liquibase.sdk.supplier.database;

import liquibase.database.Database;
import liquibase.servicelocator.ServiceLocator;

import java.util.*;

public class ConnectionConfigurationFactory {

    private static ConnectionConfigurationFactory instance;

    private Map<String, Set<ConnectionSupplier>> configsByDatabase = new HashMap<String, Set<ConnectionSupplier>>();

    public ConnectionConfigurationFactory() {
        try {
            Class[] classes = ServiceLocator.getInstance().findClasses(ConnectionSupplier.class);

            //noinspection unchecked
            for (Class<? extends ConnectionSupplier> clazz : classes) {
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

    public Set<ConnectionSupplier> getConfigurations(Database database) {
        Set<ConnectionSupplier> configurations = configsByDatabase.get(database.getShortName());
        if (configurations == null) {
            return new HashSet<ConnectionSupplier>();
        }
        return configurations;
    }

    public static void reset() {
        instance = new ConnectionConfigurationFactory();
    }

    public void register(ConnectionSupplier config) {
        String databaseShortName = config.getDatabaseShortName();
        if (!configsByDatabase.containsKey(databaseShortName)) {
            configsByDatabase.put(databaseShortName, new HashSet<ConnectionSupplier>());
        }
        configsByDatabase.get(databaseShortName).add(config);
    }

    public Collection<ConnectionSupplier> findConfigurations(String[] descriptions) throws UnknownDatabaseException {
        List<ConnectionSupplier> returnList = new ArrayList<ConnectionSupplier>();
        for (String config : descriptions) {
            Map<String, String> params = null;
            params = parseConfig(config);

            if (!params.containsKey("config")) {
                params.put("config", "standard");
            }

            String databaseName = params.get("databaseName");
            String configName = params.get("config");

            Set<ConnectionSupplier> potentialConfigurations = configsByDatabase.get(databaseName);
            if (potentialConfigurations == null) {
                throw new UnknownDatabaseException("No database configurations for "+databaseName);
            }

            boolean foundConfig = false;
            for (ConnectionSupplier potential : potentialConfigurations) {
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
