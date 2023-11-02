package liquibase.configuration;

import liquibase.Scope;
import liquibase.SingletonObject;
import liquibase.servicelocator.ServiceLocator;

import java.util.*;

/**
 * Factory for working with {@link ConfiguredValueModifier}s.
 */
public class ConfiguredValueModifierFactory  implements SingletonObject {

    private final TreeSet<ConfiguredValueModifier> allInstances;

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
        // look backwards through the treeset, thus starting with the modifier with the highest order
        for (Iterator<ConfiguredValueModifier> iterator = allInstances.descendingIterator(); iterator.hasNext(); ) {
            ConfiguredValueModifier allInstance = iterator.next();
            String overriddenValue = allInstance.override(configuredValue);
            if (!configuredValue.equals(overriddenValue)) {
                return overriddenValue;
            }
        }
        return configuredValue;
    }
}
