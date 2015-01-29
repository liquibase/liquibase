package liquibase.action.core;

import liquibase.actionlogic.core.CreateTableLogic;

import java.util.LinkedHashMap;
import java.util.Map;

public class StringClauses {

    private LinkedHashMap<String, String> clauses = new LinkedHashMap<>();

    public StringClauses append(String clause) {
        return this.append(clause, clause);
    }

    public StringClauses append(Enum name, String clause) {
        return this.append(name.name(), clause);
    }

    public StringClauses append(String name, String clause) {
        clauses.put(name, clause);
        return this;
    }

    public StringClauses remove(Enum name) {
        return remove(name.name());
    }

    public StringClauses remove(String name) {
        clauses.remove(name);
        return this;
    }

    public StringClauses replace(Enum key, String newValue) {
        return replace(key.name(), newValue);
    }

    public StringClauses replace(String key, String newValue) {
        LinkedHashMap<String, String> newMap = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : clauses.entrySet()) {
            if (entry.getKey().equals(key)) {
                newMap.put(key, newValue);
            } else {
                newMap.put(entry.getKey(), entry.getValue());
            }
        }
        this.clauses = newMap;

        return this;
    }

    public StringClauses insertBefore(String existingKey, String newValue) {
        return insertBefore(existingKey, newValue, newValue);

    }

    public StringClauses insertBefore(String existingKey, String newKey, String newValue) {
        LinkedHashMap<String, String> newMap = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : clauses.entrySet()) {
            if (entry.getKey().equals(existingKey)) {
                newMap.put(newKey, newValue);
            }
            newMap.put(entry.getKey(), entry.getValue());
        }
        this.clauses = newMap;

        return this;
    }

    public StringClauses insertAfter(Enum existingKey, String newValue) {
        return insertAfter(existingKey.name(), newValue);
    }

    public StringClauses insertAfter(String existingKey, String newValue) {
        return insertAfter(existingKey, newValue, newValue);

    }

    public StringClauses insertAfter(String existingKey, String newKey, String newValue) {
        LinkedHashMap<String, String> newMap = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : clauses.entrySet()) {
            newMap.put(entry.getKey(), entry.getValue());
            if (entry.getKey().equals(existingKey)) {
                newMap.put(newKey, newValue);
            }
        }
        this.clauses = newMap;

        return this;
    }

    public String get(Enum exitingKey) {
        return this.clauses.get(exitingKey.name());
    }

    public String get(String exitingKey) {
        return this.clauses.get(exitingKey);
    }
}
