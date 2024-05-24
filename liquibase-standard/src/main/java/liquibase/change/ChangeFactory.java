package liquibase.change;

import liquibase.ChecksumVersion;
import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.SupportsMethodValidationLevelsEnum;
import liquibase.database.Database;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.plugin.AbstractPluginFactory;
import liquibase.plugin.Plugin;
import liquibase.servicelocator.ServiceLocator;
import liquibase.util.LiquibaseUtil;
import lombok.Setter;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory class for constructing the correct liquibase.change.Change implementation based on a command name.
 * For XML-based changelogs, the tag name is the command name.
 * Change implementations are looked up via the {@link ServiceLocator}.
 *
 * @see liquibase.change.Change
 */
public class ChangeFactory extends AbstractPluginFactory<Change>{

    private final Map<String, ChangeMetaData> cachedMetadata = new ConcurrentHashMap<>();

    /**
     * Should the change be checked to see if it supports the current database?
     */
    @Setter
    private boolean performSupportsDatabaseValidation = true;

    protected static final String SUPPORTS_METHOD_REQUIRED_MESSAGE = "%s class does not implement the 'supports(Database)' method and may incorrectly override other databases changes causing unexpected behavior. Please report this to the Liquibase developers or if you are developing this change please fix it ;)";

    private ChangeFactory() {

    }

    @Override
    protected Class<Change> getPluginClass() {
        return Change.class;
    }

    @Override
    protected int getPriority(Change obj, Object... args) {
        String commandName = (String) args[0];
        ChangeMetaData changeMetaData = getChangeMetaData(obj);
        if (commandName.equals(changeMetaData.getName())) {
            return changeMetaData.getPriority();
        } else {
            return Plugin.PRIORITY_NOT_APPLICABLE;
        }
    }

    public ChangeMetaData getChangeMetaData(String change) {
        Change changeObj = create(change);
        if (changeObj == null) {
            return null;
        }
        return getChangeMetaData(changeObj);
    }

    public ChangeMetaData getChangeMetaData(Change change) {
        String cacheKey = generateCacheKey(change);
        cachedMetadata.computeIfAbsent(cacheKey, c -> change.createChangeMetaData());
        return cachedMetadata.get(cacheKey);
    }

    private String generateCacheKey(Change change) {
        String key;
        try {
            ChecksumVersion version = Scope.getCurrentScope().getChecksumVersion();
            if (version == null) {
                 throw new NullPointerException();
            }
            key = change.getClass().getName() + version;
        } catch (Exception ignored) {
            key = change.getClass().getName();
        }
        return key;
    }


    /**
     * Unregister all instances of a given Change name. Normally used for testing, but can be called manually if needed.
     */
    public void unregister(String name) {
        for (Change change : new ArrayList<>(findAllInstances())) {
            if (getChangeMetaData(change).getName().equals(name)) {
                this.removeInstance(change);
            }
        }
    }

    /**
     * Returns all defined changes in the registry. Returned set is not modifiable.
     */
    public SortedSet<String> getDefinedChanges() {
        SortedSet<String> names = new TreeSet<>();
        for (Change change : findAllInstances()) {
            names.add(getChangeMetaData(change).getName());
        }
        return Collections.unmodifiableSortedSet(names);
    }

    /**
     * Create a new Change implementation for the given change name. The class of the constructed object will be the Change implementation with the highest priority.
     * Each call to create will return a new instance of the Change.
     */
    public Change create(String name) {
        Set<Change> plugins = getPlugins(name);

        if (plugins.isEmpty()) {
            return null;
        } else if (plugins.size() > 1) {
            // we only verify supports method if we have more than 1 implementation for this change,
            // otherwise there is no use in doing that as we will use the only implementation available
            this.verifySupportsMethodImplementation(plugins);
            Database database = Scope.getCurrentScope().getDatabase();
            if (database != null && performSupportsDatabaseValidation) {
                plugins.removeIf(a -> !a.supports(database));
                if (plugins.isEmpty()) {
                    throw new UnexpectedLiquibaseException(String.format("No registered %s plugin found for %s database", name, database.getDisplayName()));
                }
            }
        }

        try {
            return plugins.iterator().next().getClass().getConstructor().newInstance();
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    /**
     * Verify if the supports method is implemented in the change if it's not part of the default liquibase changes
     */
    private void verifySupportsMethodImplementation(Set<Change> plugins) {
        if (GlobalConfiguration.SUPPORTS_METHOD_VALIDATION_LEVEL.getCurrentValue().equals(SupportsMethodValidationLevelsEnum.OFF)) {
            return;
        }
        //we only verify supports method if this is not part of the default liquibase changes
        for (Change plugin : plugins) {
            String packageName = plugin.getClass().getPackage().getName();
            if (packageName.startsWith("liquibase.change")) {
                continue;
            }

            try {
                // if the supports method is not implemented in the plugin show the warning according to the defined level
                if (plugin.getClass().getMethod("supports", Database.class).getDeclaringClass().getPackage().getName().startsWith("liquibase.change")) {
                    if (LiquibaseUtil.getBuildVersion().equals(LiquibaseUtil.DEV_VERSION)) {
                        throw new UnexpectedLiquibaseException(String.format(SUPPORTS_METHOD_REQUIRED_MESSAGE, plugin.getClass().getName()));
                    }
                    switch (GlobalConfiguration.SUPPORTS_METHOD_VALIDATION_LEVEL.getCurrentValue()) {
                        case WARN:
                            Scope.getCurrentScope().getLog(getClass()).warning(String.format(SUPPORTS_METHOD_REQUIRED_MESSAGE, plugin.getClass().getName()));
                            break;
                        case FAIL:
                            throw new UnexpectedLiquibaseException(String.format(SUPPORTS_METHOD_REQUIRED_MESSAGE, plugin.getClass().getName()));
                        default:
                            break;
                    }
                }
            } catch (NoSuchMethodException e) {
                throw new UnexpectedLiquibaseException(String.format(SUPPORTS_METHOD_REQUIRED_MESSAGE, plugin.getClass().getName()));
            }
        }
    }

    public Map<String, Object> getParameters(Change change) {
        Map<String, Object> returnMap = new HashMap<>();
        ChangeMetaData changeMetaData = getChangeMetaData(change);
        for (ChangeParameterMetaData param : changeMetaData.getParameters().values()) {
            Object currentValue = param.getCurrentValue(change);
            if (currentValue != null) {
                returnMap.put(param.getParameterName(), currentValue);
            }
        }

        return returnMap;
    }

    /**
     * @deprecated Use {@link liquibase.Scope#getSingleton(Class)}
     */
    @Deprecated
    public static ChangeFactory getInstance() {
        return Scope.getCurrentScope().getSingleton(ChangeFactory.class);
    }

}
