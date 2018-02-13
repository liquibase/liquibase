package liquibase.change;

import javafx.scene.layout.Priority;
import liquibase.Scope;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.logging.LogService;
import liquibase.logging.Logger;
import liquibase.plugin.AbstractPluginFactory;
import liquibase.plugin.Plugin;
import liquibase.servicelocator.ServiceLocator;

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

    private Map<Class<? extends Change>, ChangeMetaData> metaDataByClass = new ConcurrentHashMap<>();

    private Logger log;

    private ChangeFactory() {
      log = LogService.getLog(getClass());
    }

    protected Logger getLogger() {
      return log;
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
        if (!metaDataByClass.containsKey(change.getClass())) {
            metaDataByClass.put(change.getClass(), change.createChangeMetaData());
        }
        return metaDataByClass.get(change.getClass());
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
        Change plugin = getPlugin(name);
        if (plugin == null) {
            return null;
        }
        try {
            return plugin.getClass().newInstance();
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
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

    // exposed for test only
    protected void setLogger(Logger mockLogger) {
      log = mockLogger;
    }
}
