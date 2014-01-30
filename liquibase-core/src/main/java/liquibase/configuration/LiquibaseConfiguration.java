package liquibase.configuration;

import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides unified management of configuration properties within Liquibase core and in extensions.
 * <p>
 * This class is the top level container used to access {@link liquibase.configuration.AbstractConfiguration} implementations which contain the actual configuration properties.
 * Normal use is to call LiquibaseConfiguration.getInstance().getConfiguration(NEEDED_CONFIGURATION.class).getYOUR_PROPERTY()
 * <p>
 * This class is implemented as a singleton with a single global set of configuration objects, but the {@link #setInstance(LiquibaseConfiguration)} method can be used to replace
 * the singleton with an alternate implementation that uses ThreadLocal objects or any other way of managing configurations.
 */
public class LiquibaseConfiguration {

    private Map<Class, AbstractConfiguration> configurations;

    private ConfigurationProvider[] configurationProviders;

    private static LiquibaseConfiguration instance;

    /**
     * Returns the singleton instance.
     */
    public static LiquibaseConfiguration getInstance() {
        if (instance == null) {
            instance = new LiquibaseConfiguration();
            instance.init();
        }

        return instance;
    }

    /**
     * Overrides the standard singleton instance created by getInstance().
     * Useful for alternate implementations with more complex AbstractConfiguration lookup logic such as different configurations per thread.
     */
    public static void setInstance(LiquibaseConfiguration instance) {
        LiquibaseConfiguration.instance = instance;
    }


    /**
     * Constructor protected to prevent construction outside getInstance()
     */
    protected LiquibaseConfiguration() {
    }


    /**
     * Re-initialize the configuration with the given ConfigurationProviders. Any existing AbstractConfiguration instances are reset to
     * defaults.
     */
    public void init(ConfigurationProvider... configurationProviders) {
        if (configurationProviders == null) {
            configurationProviders = new ConfigurationProvider[0];
        }
        this.configurationProviders = configurationProviders;

        this.reset();
    }

    /**
     * Resets existing AbstractConfiguration instances to their default values.
     */
    public void reset() {
        this.configurations = new HashMap<Class, AbstractConfiguration>();
    }


    /**
     * Return an instance of the passed AbstractConfiguration type.
     * The same instance is returned from every call to getConfiguration()
     */
    public <T extends AbstractConfiguration> T getConfiguration(Class<T> type) {
        if (!configurations.containsKey(type)) {
            configurations.put(type, createConfiguration(type));
        }

        return (T) configurations.get(type);
    }

    /**
     * Convenience method for liquibaseConfiguration.getConfiguration(type).getProperty(property)
     */
    public AbstractConfiguration.ConfigurationProperty getProperty(Class<? extends AbstractConfiguration> type, String property) {
        AbstractConfiguration configuration = getConfiguration(type);
        return configuration.getProperty(property);
    }

    protected  <T extends AbstractConfiguration> T createConfiguration(Class<T> type) {
        try {
            T configuration = type.newInstance();
            configuration.init(new SystemPropertyProvider());
            return configuration;
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException("Cannot create default configuration "+type.getName(), e);
        }
    }

    /**
     * Convenience method for {@link #describeValueLookup(liquibase.configuration.AbstractConfiguration.ConfigurationProperty)}
     */
    public String describeValueLookup(Class<? extends AbstractConfiguration> config, String property) {
        return describeValueLookup(getProperty(config, property));
    }

    /**
     * Generates a human consumable description of how the configured ConfigurationProvider(s) will attempt to set a default value.
     */
    public String describeValueLookup(AbstractConfiguration.ConfigurationProperty property) {
        List<String> reasons = new ArrayList<String>();
        for (ConfigurationProvider container : configurationProviders) {
            reasons.add(container.describeDefaultLookup(property));
        }

        return StringUtils.join(reasons, " AND ");
    }
}
