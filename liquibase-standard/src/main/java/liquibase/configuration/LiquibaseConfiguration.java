package liquibase.configuration;

import liquibase.Scope;
import liquibase.SingletonObject;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.servicelocator.ServiceLocator;
import liquibase.util.StringUtil;

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

    /**
     * Orders providers by ascending {@link ConfigurationValueProvider#getPrecedence()}: resolution walks the
     * list in order and later values override earlier ones, so the last provider wins.
     */
    private static final Comparator<ConfigurationValueProvider> PROVIDER_ORDER = (o1, o2) -> {
        if (o1.getPrecedence() < o2.getPrecedence()) {
            return -1;
        } else if (o1.getPrecedence() > o2.getPrecedence()) {
            return 1;
        }

        return o1.getClass().getName().compareTo(o2.getClass().getName());
    };

    /**
     * Scope key holding a {@code List<ConfigurationValueProvider>} visible only to the current scope and its
     * children. Integrations that run executions side by side (CLI, maven) put their per-execution providers
     * here instead of {@link #registerProvider(ConfigurationValueProvider)}, so one execution's arguments never
     * leak into or vanish from another running in parallel.
     * <p>
     * A nested scope that sets this key shadows the outer list rather than combining with it; integrations set
     * it once per execution.
     */
    public static final String SCOPED_VALUE_PROVIDERS_KEY = "liquibase.scopedValueProviders";

    // Immutable snapshot replaced on every mutation, so resolution never sees a half-updated set.
    private volatile SortedSet<ConfigurationValueProvider> configurationValueProviders;
    private final static SortedSet<ConfigurationDefinition<?>> definitions = new TreeSet<>();
    public static final String REGISTERED_VALUE_PROVIDERS_KEY = "REGISTERED_VALUE_PROVIDERS";

    /**
     * Track looked up values we have logged to avoid infinite loops between this and the log system using configurations
     * and to limit logged messages.
     * Only re-log when values changed from the last time they were logged.
     */
    private final Map<String, String> lastLoggedKeyValues = new HashMap<>();

    protected LiquibaseConfiguration() {
        configurationValueProviders = new TreeSet<>(PROVIDER_ORDER);
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
    public synchronized void init(Scope scope) {
        ServiceLocator serviceLocator = scope.getServiceLocator();
        final List<AutoloadedConfigurations> containers = serviceLocator.findInstances(AutoloadedConfigurations.class);
        for (AutoloadedConfigurations container : containers) {
            Scope.getCurrentScope().getLog(getClass()).fine("Found ConfigurationDefinitions in " + container.getClass().getName());
        }

        SortedSet<ConfigurationValueProvider> replacement = new TreeSet<>(PROVIDER_ORDER);
        replacement.addAll(serviceLocator.findInstances(ConfigurationValueProvider.class));
        this.configurationValueProviders = replacement;
    }

    /**
     * Adds a new {@link ConfigurationValueProvider} to the active collection of providers.
     */
    public synchronized void registerProvider(ConfigurationValueProvider valueProvider) {
        SortedSet<ConfigurationValueProvider> replacement = new TreeSet<>(this.configurationValueProviders);
        replacement.add(valueProvider);
        this.configurationValueProviders = replacement;
    }

    /**
     * Removes the given {@link ConfigurationValueProvider} from the active collection of providers.
     *
     * @return true if the given provider was previously registered.
     */
    public synchronized boolean unregisterProvider(ConfigurationValueProvider valueProvider) {
        SortedSet<ConfigurationValueProvider> replacement = new TreeSet<>(this.configurationValueProviders);
        boolean removed = replacement.remove(valueProvider);
        this.configurationValueProviders = replacement;
        return removed;
    }

    /**
     * Removes a specific {@link ConfigurationValueProvider} from the active collection of providers.
     *
     * @return true if the provider was removed.
     */
    public synchronized boolean removeProvider(ConfigurationValueProvider provider) {
        return unregisterProvider(provider);
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
     * Returns only the globally registered providers. Callers that need everything resolution would see,
     * including the current scope's per-execution providers, want {@link #getEffectiveProviders()}.
     */
    public SortedSet<ConfigurationValueProvider> getProviders() {
        return Collections.unmodifiableSortedSet(this.configurationValueProviders);
    }

    /**
     * Returns the globally registered providers merged with the current scope's ones, ordered by
     * {@link ConfigurationValueProvider#getPrecedence()} exactly as {@link #getCurrentConfiguredValue} resolves them.
     */
    public List<ConfigurationValueProvider> getEffectiveProviders() {
        List<ConfigurationValueProvider> effective = new ArrayList<>(this.configurationValueProviders);
        List<ConfigurationValueProvider> scoped = getScopedValueProviders();
        if (scoped != null) {
            effective.addAll(scoped);
            effective.sort(PROVIDER_ORDER);
        }
        return Collections.unmodifiableList(effective);
    }

    @SuppressWarnings("unchecked")
    private List<ConfigurationValueProvider> getScopedValueProviders() {
        return Scope.getCurrentScope().get(SCOPED_VALUE_PROVIDERS_KEY, List.class);
    }

    public ConfigurationValueProvider getProvider(ConfigurationValueProvider provider) {
        return this.configurationValueProviders.stream().filter(cvp -> cvp.getClass() == provider.getClass()).findFirst().orElse(null);
    }

    /**
     * Convenience method for {@link #getCurrentConfiguredValue(ConfigurationValueConverter, ConfigurationValueObfuscator, ConfigurationValueProvider[], String...)}
     * with no additional value providers.
     */
    public <DataType> ConfiguredValue<DataType> getCurrentConfiguredValue(ConfigurationValueConverter<DataType> converter, ConfigurationValueObfuscator<DataType> obfuscator, String... keyAndAliases) {
        return this.getCurrentConfiguredValue(converter, obfuscator, null, keyAndAliases);
    }

    /**
     * Searches for the given keys in the current providers and applies any applicable modifiers.
     *
     * @param keyAndAliases The first element should be the canonical key name, with later elements being aliases. At least one element must be provided.
     * @param additionalValueProviders additional {@link ConfigurationValueProvider}s to use with higher priority than the ones registered in {@link LiquibaseConfiguration}. The higher the array index, the higher the priority. Can be null.
     * @return the value for the key, or null if not configured.
     */
    public <DataType> ConfiguredValue<DataType> getCurrentConfiguredValue(ConfigurationValueConverter<DataType> converter,
                                                                          ConfigurationValueObfuscator<DataType> obfuscator,
                                                                          ConfigurationValueProvider[] additionalValueProviders,
                                                                          String... keyAndAliases) {
        if (keyAndAliases == null || keyAndAliases.length == 0) {
            throw new IllegalArgumentException("Must specify at least one key");
        }

        ConfiguredValue<DataType> details = new ConfiguredValue<>(keyAndAliases[0], converter, obfuscator);

        List<ConfigurationValueProvider> finalValueProviders = new ArrayList<>(getEffectiveProviders());
        if (additionalValueProviders != null) {
            finalValueProviders.addAll(Arrays.asList(additionalValueProviders));
        }

        for (ConfigurationValueProvider provider : finalValueProviders) {
            final ProvidedValue providerValue = provider.getProvidedValue(keyAndAliases);

            if (providerValue != null) {
                details.override(providerValue);
            }
        }

        Scope.getCurrentScope().getSingleton(ConfiguredValueModifierFactory.class).override(details);

        final String foundValue = String.valueOf(details.getValue());
        if (!foundValue.equals(lastLoggedKeyValues.get(keyAndAliases[0]))) {
            lastLoggedKeyValues.put(keyAndAliases[0], foundValue);

            //avoid infinite loop when logging is getting set up
            if (details.found()) {
                StringBuilder logMessage = new StringBuilder("Found '" + keyAndAliases[0] + "' configuration of '" + details.getValueObfuscated() + "'");
                boolean foundFirstValue = false;
                for (ProvidedValue providedValue : details.getProvidedValues()) {
                    logMessage.append("\n    ");
                    if (foundFirstValue) {
                        logMessage.append("Overrides ");
                    }

                    //
                    // Only lower case the first character is
                    // the first two characters are NOT uppercase.  This allows provider
                    // strings like 'AWS' to be displayed correctly, i.e. as 'AWS', not 'aWS'
                    //
                    String describe = providedValue.describe();
                    char[] chars = describe.toCharArray();
                    if (chars.length >= 2 && Character.isUpperCase(chars[0]) && Character.isUpperCase(chars[1])) {
                        logMessage.append(describe);
                    } else {
                        logMessage.append(StringUtil.lowerCaseFirst(describe));
                    }
                    Object value = providedValue.getValue();
                    if (value != null) {
                        String finalValue = String.valueOf(value);
                        if (obfuscator != null) {
                            finalValue = "*****";
                        }
                        logMessage.append(" of '").append(finalValue).append("'");
                    }
                    foundFirstValue = true;
                }

                Scope.getCurrentScope().getLog(getClass()).fine(logMessage.toString());
            } else {
                Scope.getCurrentScope().getLog(getClass()).fine("No configuration value for " + StringUtil.join(keyAndAliases, " aka ") + " found");
            }
        }

        return details;
    }

    /**
     * Registers a {@link ConfigurationDefinition} so it will be returned by {@link #getRegisteredDefinitions(boolean)}
     */
    public void registerDefinition(ConfigurationDefinition<?> definition) {
        definitions.add(definition);
    }

    /**
     * Returns all registered {@link ConfigurationDefinition}s. Registered definitions are used for generated help documentation.
     * @param includeInternal if true, include {@link ConfigurationDefinition#isInternal()} definitions.
     */
    public SortedSet<ConfigurationDefinition<?>> getRegisteredDefinitions(boolean includeInternal) {
        SortedSet<ConfigurationDefinition<?>> returnSet = new TreeSet<>();
        for (ConfigurationDefinition<?> definition : definitions) {
            if (includeInternal || !definition.isInternal()) {
                returnSet.add(definition);
            }
        }

        return Collections.unmodifiableSortedSet(returnSet);
    }

    /**
     * @return the registered {@link ConfigurationDefinition} associated with this key. Null if none match.
     */
    public ConfigurationDefinition<?> getRegisteredDefinition(String key) {
        for (ConfigurationDefinition<?> def : getRegisteredDefinitions(true)) {
            List<String> keys = new ArrayList<>();
            keys.add(def.getKey());
            keys.addAll(def.getAliasKeys());

            for (String keyName : keys) {
                if (keyName.equalsIgnoreCase(key)) {
                    return def;
                }
                if (keyName.replace(".", "").equalsIgnoreCase(key)) {
                    return def;
                }
            }
            
        }
        return null;
    }
}
