package liquibase.configuration;

import liquibase.Scope;
import liquibase.SingletonObject;
import liquibase.servicelocator.ServiceLocator;

import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Provides unified management of configuration properties within Liquibase core and in extensions.
 * <p>
 * Because this class focuses on raw/untyped access to what is actually configured, it is usually best to interact with {@link ConfigurationDefinition} instances
 * which provide type safety, standardized key naming, default values, and more.
 * <p>
 * "Registered" configuration definitions will be available for generated help.
 * <p>
 * This class will search through the configured {@link ConfigurationValueProvider}s. Standard value providers are auto-loaded on startup, but more can be added/removed at runtime.
 * <p>
 */
public class LiquibaseConfiguration implements SingletonObject {

    private final SortedSet<ConfigurationValueProvider> configurationValueProviders;
    private final SortedSet<ConfigurationDefinition> definitions = new TreeSet<>();

    public static LiquibaseConfiguration getInstance() {
        return Scope.getCurrentScope().getSingleton(LiquibaseConfiguration.class);
    }

    public LiquibaseConfiguration() {
        configurationValueProviders = new TreeSet<>((o1, o2) -> {
            if (o1.getPrecedence() < o2.getPrecedence()) {
                return -1;
            } else if (o1.getPrecedence() > o2.getPrecedence()) {
                return 1;
            }

            return o1.getClass().getName().compareTo(o2.getClass().getName());
        });

    }

    /**
     * Finishes configuration of this service. Called as the root scope is set up, should not be called elsewhere.
     */
    public void init(Scope scope) {
        configurationValueProviders.clear();
        ServiceLocator serviceLocator = scope.getServiceLocator();
        final List<ConfigurationDefinitionHolder> containers = serviceLocator.findInstances(ConfigurationDefinitionHolder.class);
        for (ConfigurationDefinitionHolder container : containers) {
            Scope.getCurrentScope().getLog(getClass()).fine("Found ConfigurationDefinitions in " + container.getClass().getName());
        }

        configurationValueProviders.addAll(serviceLocator.findInstances(ConfigurationValueProvider.class));
    }

    /**
     * Adds a new {@link ConfigurationValueProvider} to the active collection of providers.
     */
    public void addProvider(ConfigurationValueProvider valueProvider) {
        this.configurationValueProviders.add(valueProvider);
    }

    /**
     * Searches for the given key in the current providers.
     *
     * @return the value for the key, or null if not configured.
     */
    public CurrentValueDetails getCurrentValue(String key) {
        CurrentValueDetails details = null;
        for (ConfigurationValueProvider provider : configurationValueProviders) {
            final CurrentValueSourceDetails providerValue = provider.getValue(key);

            if (providerValue != null) {
                if (details == null) {
                    details = new CurrentValueDetails();
                }

                details.override(providerValue);
            }
        }

        return details;
    }

    /**
     * Registers a {@link ConfigurationDefinition} so it will be returned by {@link #getRegisteredDefinitions()}
     */
    public void registerDefinition(ConfigurationDefinition definition) {
        this.definitions.add(definition);
    }

    /**
     * Returns all registered {@link ConfigurationDefinition}s. Registered definitions are used for generated help documentation.
     */
    public SortedSet<ConfigurationDefinition> getRegisteredDefinitions() {
        return Collections.unmodifiableSortedSet(this.definitions);
    }
}
