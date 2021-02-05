package liquibase.configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Detailed information about the current configuration value.
 */
public class CurrentValueDetails {

    private final List<CurrentValueSourceDetails> sourceHistory = new ArrayList<>();

    public CurrentValueDetails() {
    }

    public Object getValue() {
        if (sourceHistory.size() == 0) {
             return null;
        }
        return sourceHistory.get(0).getValue();
    }

    public void override(CurrentValueSourceDetails details) {
        this.sourceHistory.add(0, details);
    }

    /**
     * @return a full list of how the value was set and overridden.
     */
    public List<CurrentValueSourceDetails> getSourceHistory() {
        return Collections.unmodifiableList(sourceHistory);
    }
}
