package liquibase.changelog;

import liquibase.Scope;
import liquibase.database.Database;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.plugin.AbstractPluginFactory;
import liquibase.plugin.Plugin;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChangeLogHistoryServiceFactory extends AbstractPluginFactory<ChangeLogHistoryService> {

    private final List<ChangeLogHistoryService> explicitRegistered = new CopyOnWriteArrayList<>();
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
    public void register(final ChangeLogHistoryService plugin) {
        super.register(plugin);
        explicitRegistered.add(plugin);
    }


    private ChangeLogHistoryService selectFor(Database database) {
        ChangeLogHistoryService exampleService = getPlugin(
                candidate -> candidate.supports(database) ? candidate.getPriority() : Plugin.PRIORITY_NOT_APPLICABLE
        );

        try {
            Class<? extends ChangeLogHistoryService> aClass = exampleService.getClass();
            ChangeLogHistoryService service;
            try {
                aClass.getConstructor();
                service = aClass.getConstructor().newInstance();
                service.setDatabase(database);
            } catch (NoSuchMethodException e) {
                // must have been manually added to the registry and so already configured.
                service = exampleService;
            }

            return service;
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    public ChangeLogHistoryService getChangeLogService(Database database) {
        return services.computeIfAbsent(database, this::selectFor);
    }

    public void unregister(final ChangeLogHistoryService service) {
        removeInstance(service);
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

