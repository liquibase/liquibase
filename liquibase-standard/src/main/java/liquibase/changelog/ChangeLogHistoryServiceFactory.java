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

    /**
     * Removes the given service from the plugin registry, so it is no longer a candidate for
     * {@link #getChangeLogService(Database)} lookups that have not already been cached.
     */
    public synchronized void unregister(final ChangeLogHistoryService service) {
        removeInstance(service);
    }

    /**
     * Invalidates the {@link ChangeLogHistoryService} cached for just the given database, as opposed to
     * {@link #resetAll()} which invalidates and drops every database's entry. Since this factory is a
     * single JVM-wide singleton, resetAll() called from one execution's cleanup would otherwise discard
     * other concurrently running executions' (different database's) services too.
     * <p>
     * The entry is reset in place rather than removed: the service is bound to the database for that
     * database's lifetime - {@link liquibase.database.OfflineConnection} registers one that
     * {@link #getPlugin} cannot rebuild - whereas the stale data a finishing command wants dropped is
     * exactly what {@link ChangeLogHistoryService#reset()} clears.
     */
    public synchronized void resetDatabase(Database database) {
        ChangeLogHistoryService service = services.get(database);
        if (service != null) {
            service.reset();
        }
    }

    /**
     * Resets and drops every database's {@link ChangeLogHistoryService}, discovered and
     * {@link #registerForDatabase} registered alike. This is a full JVM-wide teardown; cleanup paths
     * belonging to a single execution should use {@link #resetDatabase(Database)} instead, so they
     * don't discard other concurrently running executions' services.
     */
    public synchronized void resetAll() {
        for (ChangeLogHistoryService changeLogHistoryService : findAllInstances()) {
            changeLogHistoryService.reset();
        }
        // Services registered for a database never enter the plugin registry, so findAllInstances()
        // above does not cover them. reset() is idempotent, so overlap between the two is harmless.
        services.values().forEach(ChangeLogHistoryService::reset);
        services.clear();
        // unregister all self-registered
        explicitRegistered.forEach(this::removeInstance);
        explicitRegistered.clear();
    }
}

