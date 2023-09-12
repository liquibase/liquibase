package liquibase.configuration;

import liquibase.Scope;
import liquibase.SingletonObject;
import liquibase.servicelocator.ServiceLocator;

import java.util.*;

/**
 * Factory for working with {@link ConfiguredValueModifier}s.
 */
public class ConfiguredValueModifierFactory  implements SingletonObject {

    private final SortedSet<ConfiguredValueModifier> allInstances;

    private ConfiguredValueModifierFactory() {
        this.allInstances = new TreeSet<>(Comparator.comparingInt(ConfiguredValueModifier::getOrder));

        ServiceLocator serviceLocator = Scope.getCurrentScope().getServiceLocator();
        this.allInstances.addAll(serviceLocator.findInstances(ConfiguredValueModifier.class));

    }

    public void register(ConfiguredValueModifier modifier) {
        allInstances.add(modifier);
    }

    public void unregister(ConfiguredValueModifier modifier) {
        allInstances.remove(modifier);
    }

    public void override(ConfiguredValue configuredValue) {
        for (ConfiguredValueModifier modifier: allInstances) {
            modifier.override(configuredValue);
        }
    }

    public String override(String configuredValue) {
        // get the last instance because it will have the highest order, and would supercede all others
        return allInstances.last().override(configuredValue);
    }
}
