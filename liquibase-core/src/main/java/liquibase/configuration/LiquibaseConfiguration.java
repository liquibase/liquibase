package liquibase.configuration;

import liquibase.Scope;
import liquibase.SingletonObject;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.servicelocator.ServiceLocator;

import java.util.*;

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

    /**
     * Track looked up values we have logged to avoid infinite loops between this and the log system using configurations
     * and to limit logged messages.
     * Only re-log when values changed from the last time they were logged.
     */
    private final Map<String, String> lastLoggedKeyValues = new HashMap<>();

    protected LiquibaseConfiguration() {
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
     * @deprecated use {@link Scope#getSingleton(Class)}
     */
    public static LiquibaseConfiguration getInstance() {
        return Scope.getCurrentScope().getSingleton(LiquibaseConfiguration.class);
    }

    /**
     * Finishes configuration of this service. Called as the root scope is set up, should not be called elsewhere.
     */
    public void init(Scope scope) {
        configurationValueProviders.clear();
        ServiceLocator serviceLocator = scope.getServiceLocator();
        final List<AutoloadedConfigurations> containers = serviceLocator.findInstances(AutoloadedConfigurations.class);
        for (AutoloadedConfigurations container : containers) {
            Scope.getCurrentScope().getLog(getClass()).fine("Found ConfigurationDefinitions in " + container.getClass().getName());
        }

        configurationValueProviders.addAll(serviceLocator.findInstances(ConfigurationValueProvider.class));
    }

    /**
     * Adds a new {@link ConfigurationValueProvider} to the active collection of providers.
     */
    public void registerProvider(ConfigurationValueProvider valueProvider) {
        this.configurationValueProviders.add(valueProvider);
    }

    /**
     * Removes the given {@link ConfigurationValueProvider} from the active collection of providers.
     *
     * @return true if the given provider was previously registered.
     */
    public boolean unregisterProvider(ConfigurationValueProvider valueProvider) {
        return this.configurationValueProviders.remove(valueProvider);
    }

    /**
     * Removes a specific {@link ConfigurationValueProvider} from the active collection of providers.
     *
     * @return true if the provider was removed.
     */
    public boolean removeProvider(ConfigurationValueProvider provider) {
        return this.configurationValueProviders.remove(provider);
    }

    /**
     * @deprecated use {@link ConfigurationDefinition} instances directly
     */
    public <T extends ConfigurationContainer> T getConfiguration(Class<T> type) {
        try {
            return type.newInstance();
        } catch (Throwable e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }


    /**
     * Searches for the given keys in the current providers.
     * @param keyAndAliases The first element should be the canonical key name, with later elements being aliases. At least one element must be provided.
     *
     * @return the value for the key, or null if not configured.
     */
    public ConfiguredValue<?> getCurrentConfiguredValue(String... keyAndAliases) {
        if (keyAndAliases == null || keyAndAliases.length == 0) {
            throw new IllegalArgumentException("Must specify at least one key");
        }

        ConfiguredValue<?> details = new ConfiguredValue<>(keyAndAliases[0]);

        for (ConfigurationValueProvider provider : configurationValueProviders) {
            final ProvidedValue providerValue = provider.getProvidedValue(keyAndAliases);

            if (providerValue != null) {
                details.override(providerValue);
            }
        }

        final String foundValue = String.valueOf(details.getValue());
        if (!foundValue.equals(lastLoggedKeyValues.get(keyAndAliases[0]))) {
            lastLoggedKeyValues.put(keyAndAliases[0], foundValue);

            //avoid infinite loop when logging is getting set up
            StringBuilder logMessage = new StringBuilder("Found '" + keyAndAliases[0] + "' configuration of '"+foundValue+"'");
            boolean foundFirstValue = false;
            for (ProvidedValue providedValue : details.getProvidedValues()) {
                logMessage.append("\n    ");
                if (foundFirstValue) {
                    logMessage.append("Overrides ");
                }
                logMessage.append(providedValue.describe());
                final Object value = providedValue.getValue();
                if (value != null) {
                    logMessage.append(" of '").append(providedValue.getValue()).append("'");
                }
                foundFirstValue = true;
            }

            Scope.getCurrentScope().getLog(getClass()).fine(logMessage.toString());
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

    /**
     * @return the registered {@link ConfigurationDefinition} asssociated with this key. Null if none match.
     */
    public ConfigurationDefinition getRegisteredDefinition(String key) {
        for (ConfigurationDefinition def : getRegisteredDefinitions()) {
            if (def.getKey().equalsIgnoreCase(key)) {
                return def;
            }
            final Set aliasKeys = def.getAliasKeys();
            if (aliasKeys != null && aliasKeys.contains(def.getKey())) {
                return def;
            }
        }

        return null;
    }
}
