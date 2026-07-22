package liquibase.configuration;

import liquibase.Scope;
import liquibase.SingletonObject;
import liquibase.servicelocator.ServiceLocator;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Factory for working with {@link ConfiguredValueModifier}s.
 */
public class ConfiguredValueModifierFactory  implements SingletonObject {

    // Modifiers are ordered by getOrder() ascending, tie-broken by class name so iteration is deterministic
    // across JVM runs. Deliberately NOT a comparator-keyed TreeSet: that treats two modifiers with the same
    // getOrder() as duplicates and silently keeps only the first — a subtle, hard-to-diagnose failure (a
    // dropped value modifier lets a reference string reach the database as a literal value). Here same-order
    // modifiers coexist; de-duplication is by object identity.
    private static final Comparator<ConfiguredValueModifier> ORDER_COMPARATOR =
            Comparator.<ConfiguredValueModifier>comparingInt(ConfiguredValueModifier::getOrder)
                    .thenComparing(modifier -> modifier.getClass().getName());

    // Mutable backing set, guarded by 'this'. Reads use the immutable, fully-sorted snapshot below,
    // published atomically via an AtomicReference (the snapshot is never mutated in place, only replaced).
    private final List<ConfiguredValueModifier> registered = new ArrayList<>();
    private final AtomicReference<List<ConfiguredValueModifier>> sorted = new AtomicReference<>(Collections.emptyList());

    private ConfiguredValueModifierFactory() {
        ServiceLocator serviceLocator = Scope.getCurrentScope().getServiceLocator();
        // findInstances returns one instance per registered type (no duplicates), so add directly and publish.
        registered.addAll(serviceLocator.findInstances(ConfiguredValueModifier.class));
        publish();
    }

    public synchronized void register(ConfiguredValueModifier modifier) {
        if (modifier == null) {
            return;
        }
        // De-duplicate by identity so the same instance isn't added twice; distinct same-order modifiers coexist.
        for (ConfiguredValueModifier existing : registered) {
            if (existing == modifier) {
                return;
            }
        }
        registered.add(modifier);
        publish();
    }

    public synchronized void unregister(ConfiguredValueModifier modifier) {
        if (registered.removeIf(existing -> existing == modifier)) {
            publish();
        }
    }

    public void override(ConfiguredValue configuredValue) {
        for (ConfiguredValueModifier modifier : sorted.get()) {
            modifier.override(configuredValue);
        }
    }

    public String override(String configuredValue) {
        // Apply the highest-order modifiers first (reverse of the ascending sort), stopping at the first
        // that changes the value. Objects.equals so a null value a modifier leaves as null counts as
        // unchanged and the next modifier still runs, rather than short-circuiting on the first modifier.
        final List<ConfiguredValueModifier> snapshot = sorted.get();
        for (int i = snapshot.size() - 1; i >= 0; i--) {
            String overriddenValue = snapshot.get(i).override(configuredValue);
            if (!Objects.equals(configuredValue, overriddenValue)) {
                return overriddenValue;
            }
        }
        return configuredValue;
    }

    private void publish() {
        List<ConfiguredValueModifier> copy = new ArrayList<>(registered);
        copy.sort(ORDER_COMPARATOR);
        sorted.set(Collections.unmodifiableList(copy));
    }
}
