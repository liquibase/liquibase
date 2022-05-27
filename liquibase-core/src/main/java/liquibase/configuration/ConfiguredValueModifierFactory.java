package liquibase.configuration;

import liquibase.plugin.AbstractPluginFactory;

import java.util.*;

public class ConfiguredValueModifierFactory extends AbstractPluginFactory<ConfiguredValueModifier> {

    private ConfiguredValueModifierFactory() {}

    @Override
    protected Class<ConfiguredValueModifier> getPluginClass() {
        return ConfiguredValueModifier.class;
    }

    @Override
    protected void removeInstance(ConfiguredValueModifier instance) {
        super.removeInstance(instance);
    }

    @Override
    protected int getPriority(ConfiguredValueModifier obj, Object... args) {
        return obj.getPriority();
    }

    List<ConfiguredValueModifier> getModifiers() {
        List<ConfiguredValueModifier> foundModifiers = new ArrayList<>();
        foundModifiers.addAll(findAllInstances());
        foundModifiers.sort(new SortByPriority());
        return foundModifiers;
    }
}

class SortByPriority implements Comparator<ConfiguredValueModifier> {
    @Override
    public int compare(ConfiguredValueModifier a, ConfiguredValueModifier b) {
        return a.getPriority() - b.getPriority();
    }
}
