package liquibase.configuration;

import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LiquibaseConfiguration {

    private Map<Class, AbstractConfiguration> configurations = new HashMap<Class, AbstractConfiguration>();

    private ConfigurationProvider[] valueContainers;

    public LiquibaseConfiguration(ConfigurationProvider... valueContainers) {
        if (valueContainers == null) {
            valueContainers = new ConfigurationProvider[0];
        }
        this.valueContainers = valueContainers;
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
        for (ConfigurationProvider container : valueContainers) {
            reasons.add(container.describeDefaultLookup(property));
        }

        return StringUtils.join(reasons, " AND ");
    }
}
