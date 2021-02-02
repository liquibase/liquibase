package liquibase.configuration;

/**
 * Defines how a value was set.
 */
public class CurrentValueSourceDetails {
    private final String key;
    private final String source;
    private final Object value;

    public CurrentValueSourceDetails(Object value, String source, String key) {
        this.value = value;
        this.key = key;
        this.source = source;
    }

    public Object getValue() {
        return value;
    }

    public String getKey() {
        return key;
    }

    public String getSource() {
        return source;
    }

    public String describe() {
        return source + " '" + key + "'";
    }
}
