package liquibase.changelog;

import liquibase.util.StringUtils;

import java.util.List;

public class ChangeLogParameter {
    private String key;
    private Object value;
    private List<String> databases;

    public ChangeLogParameter(String key, Object value) {
        this.key = key;
        this.value = value;
    }

    public ChangeLogParameter(String key, Object value, String databases) {
        this(key, value, StringUtils.splitAndTrim(databases, ","));
    }

    public ChangeLogParameter(String key, Object value, List<String> databases) {
        this.key = key;
        this.value = value;
        this.databases = databases;
    }

    public String getKey() {
        return key;
    }

    public Object getValue() {
        return value;
    }

    public List<String> getDatabases() {
        return databases;
    }

    @Override
    public String toString() {
        return getValue().toString();
    }
}
