package liquibase.configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Detailed, type-safe information about the current configuration value that can be provided by {@link ConfigurationDefinition}.
 */
public class CurrentValue<DataType> {

    private final DataType value;
    private final List<CurrentValueSourceDetails> sourceHistory = new ArrayList<>();
    private final boolean defaultValueUsed;

    public CurrentValue(DataType value, List<CurrentValueSourceDetails> sourceHistory, boolean defaultValueUsed) {
        this.value = value;
        this.sourceHistory.addAll(sourceHistory);
        this.defaultValueUsed = defaultValueUsed;
    }

    /**
     * @return the current value. Can be null if not set and no default value.
     */
    public DataType getValue() {
        return value;
    }

    /**
     * @return true if the default value is being used rather than a configuration value
     */
    public boolean getDefaultValueUsed() {
        return defaultValueUsed;
    }

    /**
     * @return Returns where/how the value was set.
     */
    public CurrentValueSourceDetails getSource() {
        if (sourceHistory.size()== 0) {
            return null;
        }
        return sourceHistory.get(0);
    }

    /**
     * @return a full list of how the value was set and overridden.
     */
    public List<CurrentValueSourceDetails> getSourceHistory() {
        return Collections.unmodifiableList(sourceHistory);
    }

}
