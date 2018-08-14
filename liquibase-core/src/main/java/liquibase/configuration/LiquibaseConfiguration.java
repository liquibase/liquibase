package liquibase.configuration;

import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.util.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides unified management of configuration properties within Liquibase core and in extensions.
 * <p>
 * This class is the top level container used to access {@link ConfigurationContainer} implementations which contain
 * the actual configuration properties.
 * Normal use is to call
 * LiquibaseConfiguration.getInstance().getConfiguration(NEEDED_CONFIGURATION.class).getYOUR_PROPERTY()
 * <p>
 * This class is implemented as a singleton with a single global set of configuration objects, but the
 * {@link #setInstance(LiquibaseConfiguration)} method can be used to replace
 * the singleton with an alternate implementation that uses ThreadLocal objects or any other way of managing
 * configurations.
 */
public class LiquibaseConfiguration {

    private Map<Class, ConfigurationContainer> configurations;

    private ConfigurationValueProvider[] configurationValueProviders;

    private static LiquibaseConfiguration instance;

    /**
     * Returns the singleton instance, creating it if necessary. On creation, the configuration is initialized with {@link liquibase.configuration.SystemPropertyProvider}
     */
    public static synchronized LiquibaseConfiguration getInstance() {
        if (instance == null) {
            instance = new LiquibaseConfiguration();
            instance.init(new SystemPropertyProvider());
        }

        return instance;
    }

    /**
     * Overrides the standard singleton instance created by getInstance().
     * Useful for alternate implementations with more complex AbstractConfigurationContainer lookup logic such
     * as different configurations per thread.
     */
    public static synchronized void setInstance(LiquibaseConfiguration instance) {
        LiquibaseConfiguration.instance = instance;
    }


    /**
     * Constructor protected to prevent construction outside getInstance()
     */
    protected LiquibaseConfiguration() {
    }


    /**
     * Re-initialize the configuration with the given ConfigurationProviders. Any existing
     * AbstractConfigurationContainer instances are reset to defaults.
     */
    public void init(ConfigurationValueProvider... configurationValueProviders) {
        if (configurationValueProviders == null) {
            configurationValueProviders = new ConfigurationValueProvider[0];
        }
        this.configurationValueProviders = configurationValueProviders;

        this.reset();
    }

    /**
     * Resets existing AbstractConfigurationContainer instances to their default values.
     */
    public void reset() {
        this.configurations = new HashMap<>();
    }


    /**
     * Return an instance of the passed AbstractConfigurationContainer type.
     * The same instance is returned from every call to getConfiguration()
     */
    public <T extends ConfigurationContainer> T getConfiguration(Class<T> type) {
        if (!configurations.containsKey(type)) {
            configurations.put(type, createConfiguration(type));
        }

        return (T) configurations.get(type);
    }

    public ConfigurationContainer getConfiguration(String typeName) {
        for (Map.Entry<Class, ConfigurationContainer> entry : configurations.entrySet()) {
            if (entry.getKey().getName().equals(typeName)) {
                return entry.getValue();
            }
        }
        try {
            Class typeClass = Class.forName(typeName);
            configurations.put(typeClass, createConfiguration(typeClass));
            return configurations.get(typeClass);
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    /**
     * Convenience method for liquibaseConfiguration.getConfiguration(type).getProperty(property)
     */
    public ConfigurationProperty getProperty(Class<? extends ConfigurationContainer> type, String property) {
        ConfigurationContainer configuration = getConfiguration(type);
        return configuration.getProperty(property);
    }

    protected  <T extends ConfigurationContainer> T createConfiguration(Class<T> type) {
        try {
            T configuration = type.newInstance();
            configuration.init(configurationValueProviders);
            return configuration;
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException("Cannot create default configuration "+type.getName(), e);
        }
    }

    /**
     * Convenience method for {@link #describeValueLookupLogic(ConfigurationProperty)}
     */
    public String describeValueLookupLogic(Class<? extends ConfigurationContainer> config, String property) {
        return describeValueLookupLogic(getProperty(config, property));
    }

    /**
     * Generates a human consumable description of how the configured ConfigurationValueProvider(s) will
     * attempt to set a default value.
     */
    public String describeValueLookupLogic(ConfigurationProperty property) {
        List<String> reasons = new ArrayList<>();
        for (ConfigurationValueProvider container : configurationValueProviders) {
            reasons.add(container.describeValueLookupLogic(property));
        }

        return StringUtil.join(reasons, " AND ");
    }
}
