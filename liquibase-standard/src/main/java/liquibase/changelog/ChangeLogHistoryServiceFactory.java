package liquibase.changelog;

import liquibase.Scope;
import liquibase.database.Database;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.plugin.AbstractPluginFactory;
import liquibase.plugin.Plugin;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChangeLogHistoryServiceFactory extends AbstractPluginFactory<ChangeLogHistoryService> {

    private final List<ChangeLogHistoryService> explicitRegistered = new ArrayList<>();
    private final Map<Database, ChangeLogHistoryService> services = new ConcurrentHashMap<>();

    /**
     * @deprecated Instead use Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class)
     */
    @Deprecated
    public static synchronized ChangeLogHistoryServiceFactory getInstance() {
        return Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class);
    }

    private ChangeLogHistoryServiceFactory() {
    }

    @Override
    protected Class<ChangeLogHistoryService> getPluginClass() {
        return ChangeLogHistoryService.class;
    }

    @Override
    protected int getPriority(final ChangeLogHistoryService changeLogHistoryService, final Object... args) {
        Database database = (Database) args[0];
        if (changeLogHistoryService.supports(database)) {
            return changeLogHistoryService.getPriority();
        } else {
            return Plugin.PRIORITY_NOT_APPLICABLE;
        }
    }

    @Override
    public synchronized void register(final ChangeLogHistoryService plugin) {
        super.register(plugin);
        explicitRegistered.add(plugin);
    }


    /**
     * Registers a {@link ChangeLogHistoryService} that is already fully configured for the given database
     * (e.g. an offline/simulated connection's history service), bypassing {@link #getPlugin(Object...)}'s
     * generic type/priority-based lookup. That lookup can't distinguish between multiple pre-configured
     * instances of the same class and priority registered concurrently for different databases - it only
     * ever kept one, handing it out for every database.
     */
    public synchronized void registerForDatabase(Database database, ChangeLogHistoryService service) {
        services.put(database, service);
    }

    public synchronized ChangeLogHistoryService getChangeLogService(Database database) {
            if (services.containsKey(database)) {
                return services.get(database);
            }

            ChangeLogHistoryService plugin = getPlugin(database);

            if (plugin == null) {
                throw new UnexpectedLiquibaseException("Cannot find ChangeLogHistoryService for " +
                    database.getShortName());
            }

            try {
                Class<? extends ChangeLogHistoryService> aClass = plugin.getClass();
                ChangeLogHistoryService service;
                try {
                    aClass.getConstructor();
                    service = aClass.getConstructor().newInstance();
                    service.setDatabase(database);
                } catch (NoSuchMethodException e) {
                    // must have been manually added to the registry and so already configured.
                    service = plugin;
                }

                services.put(database, service);
                return service;
            } catch (Exception e) {
                throw new UnexpectedLiquibaseException(e);
            }
    }

    public synchronized void unregister(final ChangeLogHistoryService service) {
        removeInstance(service);
    }

    /**
     * Clears the cached {@link ChangeLogHistoryService} for just the given database, as opposed to
     * {@link #resetAll()} which clears every database's entry. Since this factory is a single JVM-wide
     * singleton, resetAll() called from one execution's cleanup would otherwise drop other concurrently
     * running executions' (different database's) cached services too.
     */
    public synchronized void resetDatabase(Database database) {
        services.remove(database);
    }

    public synchronized void resetAll() {
        for (ChangeLogHistoryService changeLogHistoryService : findAllInstances()) {
            changeLogHistoryService.reset();
        }
        services.clear();
        // unregister all self-registered
        explicitRegistered.forEach(this::removeInstance);
        explicitRegistered.clear();
    }
}

