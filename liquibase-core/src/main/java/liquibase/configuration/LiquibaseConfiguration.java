package liquibase.configuration;

import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LiquibaseConfiguration {

    private Map<Class, AbstractConfiguration> configurations;

    private ConfigurationProvider[] configurationProviders;

    private static LiquibaseConfiguration instance;

    public static LiquibaseConfiguration getInstance() {
        if (instance == null) {
            instance = new LiquibaseConfiguration();
            instance.init();
        }

        return instance;
    }

    public void init(ConfigurationProvider... configurationProviders) {
        if (configurationProviders == null) {
            configurationProviders = new ConfigurationProvider[0];
        }
        this.configurationProviders = configurationProviders;

        this.reset();
    }

    public static void setInstance(LiquibaseConfiguration instance) {
        LiquibaseConfiguration.instance = instance;
    }

    private LiquibaseConfiguration() {
    }

    public <T extends AbstractConfiguration> T getConfiguration(Class<T> type) {
        if (!configurations.containsKey(type)) {
            configurations.put(type, createConfiguration(type));
        }

        return (T) configurations.get(type);
    }

    protected  <T extends AbstractConfiguration> T createConfiguration(Class<T> type) {
        try {
            T configuration = type.newInstance();
            configuration.init(new SystemPropertyProvider());
            return configuration;
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException("Cannot create default configuration "+type.getName());
        }
    }

    public String describeDefaultLookup(AbstractConfiguration.ConfigurationProperty property) {
        List<String> reasons = new ArrayList<String>();
        for (ConfigurationProvider container : configurationProviders) {
            reasons.add(container.describeDefaultLookup(property));
        }

        return StringUtils.join(reasons, " AND ");
    }

    public void reset() {
        this.configurations = new HashMap<Class, AbstractConfiguration>();
    }
}
