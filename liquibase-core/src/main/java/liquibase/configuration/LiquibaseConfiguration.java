package liquibase.configuration;

import liquibase.Scope;
import liquibase.SingletonObject;
import liquibase.servicelocator.ServiceLocator;

import java.util.*;

/**
 * Provides unified management of configuration properties within Liquibase core and in extensions.
 * <p>
 * This class is the top level container used to access {@link AutoloadedConfigurations} implementations which contain
 * the actual configuration properties.
 * Normal use is to call
 * LiquibaseConfiguration.getInstance().getConfiguration(NEEDED_CONFIGURATION.class).getYOUR_PROPERTY()
 * <p>
 * This class is implemented as a singleton with a single global set of configuration objects, but the
 * {@link #setInstance(LiquibaseConfiguration)} method can be used to replace
 * the singleton with an alternate implementation that uses ThreadLocal objects or any other way of managing
 * configurations.
 */
public class LiquibaseConfiguration implements SingletonObject {

    private final SortedSet<ConfigurationValueProvider> configurationValueProviders;
    private final SortedSet<ConfigurationDefinition> definitions = new TreeSet<>();

    public static LiquibaseConfiguration getInstance() {
        return Scope.getCurrentScope().getSingleton(LiquibaseConfiguration.class);
    }

    /**
     * Constructor protected to prevent construction outside getInstance()
     */
    public LiquibaseConfiguration() {
        configurationValueProviders = new TreeSet<>((o1, o2) -> {
            if (o1.getPrecedence() < o1.getPrecedence()) {
                return -1;
            } else if (o1.getPrecedence() > o1.getPrecedence()) {
                return 1;
            }

            return o1.getClass().getName().compareTo(o2.getClass().getName());
        });

    }


    /**
     * Re-initialize the configuration with the given ConfigurationProviders. Any existing
     * AbstractConfigurationContainer instances are reset to defaults.
     */
    public void init(Scope scope) {
        ServiceLocator serviceLocator = scope.getServiceLocator();
        final List<AutoloadedConfigurations> containers = serviceLocator.findInstances(AutoloadedConfigurations.class);
        for (AutoloadedConfigurations container : containers) {
            Scope.getCurrentScope().getLog(getClass()).fine("Found ConfigurationDefinitions in "+container.getClass().getName());
        }

        configurationValueProviders.addAll(serviceLocator.findInstances(ConfigurationValueProvider.class));
    }

    //TODO: remove
    public void reset() {
    }

    public ConfigurationDefinition getDefinition(String property) {
        for (ConfigurationDefinition definition : definitions) {
            if (definition.getProperty().equals(property)) {
                return definition;
            }
        }
        return null;
    }

    public Object getCurrentValue(String property) {
        for (ConfigurationValueProvider provider : configurationValueProviders) {
            final Object value = provider.getValue(property);
            if (value != null) {
                return value;
            }
        }

        return null;
    }

    public void addDefinition(ConfigurationDefinition definition) {
        this.definitions.add(definition);
    }

    public SortedSet<ConfigurationDefinition> getDefinitions() {
        return Collections.unmodifiableSortedSet(this.definitions);
    }
}
