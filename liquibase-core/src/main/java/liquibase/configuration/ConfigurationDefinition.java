package liquibase.configuration;

import liquibase.Scope;
import liquibase.util.ObjectUtil;

import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

public class ConfigurationDefinition<DataType> implements Comparable {

    private String property;
    private Set<String> aliases = new TreeSet<>();
    private Class<DataType> type;
    private String description;
    private DataType defaultValue;
    private boolean commonlyUsed;
    private ConfigurationValueHandler<DataType> valueHandler;
    private ConfigurationValueObfuscator<DataType> valueObfuscator;

    public ConfigurationDefinition(String property, Class<DataType> type) {
        this.property = property;
        this.type = type;
        this.valueHandler = value -> ObjectUtil.convert(value, type);
    }

    public DataType getCurrentValue() {
        return getCurrentValueDetails().value;
    }

    public DataType getCurrentValueObfuscated() {
        final DataType currentValue = getCurrentValue();

        if (this.valueObfuscator == null) {
            return currentValue;
        }

        return this.valueObfuscator.obfuscate(currentValue);
    }

    public CurrentValueDetails<DataType> getCurrentValueDetails() {
        CurrentValueDetails<DataType> details = new CurrentValueDetails<>();

        final Object configurationValue = Scope.getCurrentScope().getSingleton(LiquibaseConfiguration.class).getCurrentValue(this.getProperty());
        if (configurationValue == null) {
            details.value = this.getDefaultValue();
            details.wasOverridden = false;
        } else {
            details.value = valueHandler.convert(configurationValue);
            details.wasOverridden = true;
        }

        return details;
    }

    public String getProperty() {
        return property;
    }

    public Set<String> getAliases() {
        return aliases;
    }

    public Class<DataType> getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public DataType getDefaultValue() {
        return defaultValue;
    }

    public boolean getCommonlyUsed() {
        return commonlyUsed;
    }

    @Override
    public int compareTo(Object o) {
        return this.getProperty().compareTo(((ConfigurationDefinition) o).getProperty());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConfigurationDefinition<?> that = (ConfigurationDefinition<?>) o;
        return Objects.equals(property, that.property);
    }

    @Override
    public int hashCode() {
        return Objects.hash(property);
    }

    public static class Builder {
        private String namespace;

        public Builder(String namespace) {
            this.namespace = namespace;
        }

        public <T> NewDefinition<T> define(String key, Class<T> dataType) {
            final ConfigurationDefinition<T> definition = new ConfigurationDefinition<>(namespace + "." + key, dataType);

            return new NewDefinition<>(definition);
        }

        public static class NewDefinition<DataType> {
            private ConfigurationDefinition<DataType> definition;

            private NewDefinition(ConfigurationDefinition<DataType> definition) {
                this.definition = definition;
            }

            public NewDefinition<DataType> addAlias(String alias) {
                definition.aliases.add(alias);

                return this;
            }

            public NewDefinition<DataType> setDescription(String description) {
                definition.description = description;
                return this;
            }

            public NewDefinition<DataType> setDefaultValue(DataType defaultValue) {
                definition.defaultValue = defaultValue;
                return this;
            }

            public NewDefinition<DataType> setValueHandler(ConfigurationValueHandler<DataType> handler) {
                definition.valueHandler = handler;

                return this;
            }

            public NewDefinition<DataType> setValueObfuscator(ConfigurationValueObfuscator<DataType> handler) {
                definition.valueObfuscator = handler;

                return this;
            }

            public NewDefinition<DataType> setCommonlyUsed(boolean commonlyUsed) {
                definition.commonlyUsed = commonlyUsed;

                return this;
            }

            public ConfigurationDefinition<DataType> build() {
                Scope.getCurrentScope().getSingleton(LiquibaseConfiguration.class).addDefinition(definition);

                return definition;
            }
        }


    }


}
