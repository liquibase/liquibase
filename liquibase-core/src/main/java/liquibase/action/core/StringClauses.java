package liquibase.action.core;

import liquibase.util.StringUtils;
import liquibase.util.Validate;

import java.util.*;

/**
 * Defines a list of clauses that can be manipulated and rearranged based on keys associated with each.
 * On {@link #toString()}, the clauses will be joined by the separator passed at construction time and prepended/postpended by the start/end strings set at construction time.
 * <br>
 * Note: all keys and values passed in are trimmed.<br>
 * NOTE: All keys are managed in a case INSENSITIVE manner<br>
 * NOTE: Null or empty clauses and subclauses are skipped in {@link #toString()}
 */
public class StringClauses {

    private final String separator;
    private final String start;
    private final String end;
    private LinkedHashMap<String, Object> clauses = new LinkedHashMap<>();

    /**
     * Creates a new StringClause with no start or end strings and a space separator.
     */
    public StringClauses() {
        this(null);
    }

    /**
     * Creates a new StringClause with no start or end strings and the given separator.
     */
    public StringClauses(String separator) {
        this(null, separator, null);
    }

    /**
     * Creates a new StringClause with the given start, end and separator.
     */
    public StringClauses(String start, String separator, String end) {
        if (start == null) {
            start = "";
        }
        if (separator == null) {
            separator = " ";
        }
        if (end == null) {
            end = "";
        }

        this.start = start;
        this.end = end;
        this.separator = separator;
    }

    /**
     * Adds a new clause at the end of the list with the given key.
     */
    public StringClauses append(String key, String clause) {
        key = StringUtils.trimToEmpty(key).toLowerCase();
        clause = StringUtils.trimToEmpty(clause).toLowerCase();
        if (key.equals("")) {
            key = clause;
        }
        if (key.equals("") || clause.equals("")) {
            return this;
        }
        clauses.put(key, clause.trim());
        return this;
    }

    /**
     * Adds a new sub-clause at the end of the list with the given key.
     */
    public StringClauses append(String key, StringClauses subclauses) {
        key = StringUtils.trimToEmpty(key).toLowerCase();
        Validate.notNull(key, "key must be a non-null, non-empty value");

        clauses.put(key, subclauses);
        return this;
    }

    /**
     * Adds a new clause at the end of the list. The key is equal to the passed clause.
     * There is no corresponding append with just a StringClauses because the subclause may not be fully created when appended and so the key may be an unexpected value.
     */
    public StringClauses append(String clause) {
        return this.append(clause, clause);
    }

    /**
     * Convenience method for {@link #append(String, StringClauses)} when using enums.
     */
    public StringClauses append(Enum name, StringClauses subclauses) {
        return this.append(name.name(), subclauses);
    }

    /**
     * Convenience method for {@link #append(String, String)} when using enums.
     */
    public StringClauses append(Enum name, String clause) {
        return this.append(name.name(), clause);
    }

    /**
     * Adds a clause with the given key to the beginning of the list.
     */
    public StringClauses prepend(String key, String clause) {
        return prependImpl(key, StringUtils.trimToEmpty(clause));
    }

    /**
     * Adds a sub-clause with the given key to the beginning of the list.
     */
    public StringClauses prepend(String key, StringClauses clause) {
        return prependImpl(key, clause);
    }

    /**
     * Convenience method for {@link #prepend(String, String)} that uses the clause as the key.
     */
    public StringClauses prepend(String clause) {
        return prepend(clause, clause);
    }

    /**
     * Convenience method for {@link #prepend(String, String)} when using enums
     */
    public StringClauses prepend(Enum key, String clause) {
        return prepend(key.name(), clause);
    }

    /**
     * Convenience method for {@link #prepend(String, liquibase.action.core.StringClauses)} when using enums
     */
    public StringClauses prepend(Enum key, StringClauses clause) {
        return prepend(key.name(), clause);
    }


    private StringClauses prependImpl(String key, Object clause) throws IllegalArgumentException {
        key = StringUtils.trimToEmpty(key).toLowerCase();
        if (key.equals("")) {
            throw new IllegalArgumentException("Cannot specify a null or empty key");
        }

        LinkedHashMap<String, Object> newMap = new LinkedHashMap<>();
        newMap.put(key, clause);
        newMap.putAll(this.clauses);
        this.clauses = newMap;

        return this;
    }


    /**
     * Removes the clause with the given key. Removes case insensitively. If key doesn't exist, operation is a no-op.
     */
    public StringClauses remove(String key) {
        clauses.remove(key.toLowerCase());
        return this;
    }

    /**
     * Convenience method for {@link #remove(String)} when using enums.
     */
    public StringClauses remove(Enum key) {
        return remove(key.name());
    }


    /**
     * Replaces the given key with a new string. If the existing key does not exist, throws IllegalArgumentException
     */
    public StringClauses replace(String key, String newValue) throws IllegalArgumentException {
        return replaceImpl(key, StringUtils.trimToEmpty(newValue));
    }

    /**
     * Replaces the given key with a new sub-clause. If the existing key does not exist, throws IllegalArgumentException
     */
    public StringClauses replace(String key, StringClauses newValue) {
        return replaceImpl(key, newValue);
    }

    /**
     * Convenience method for {@link #replace(String, liquibase.action.core.StringClauses)} when using enums.
     */
    public StringClauses replace(Enum key, StringClauses newValue) {
        return replace(key.name(), newValue);
    }

    /**
     * Convenience method for {@link #replace(String, String)} when using enums.
     */
    public StringClauses replace(Enum key, String newValue) {
        return replace(key.name(), newValue);
    }


    private StringClauses replaceImpl(String key, Object newValue) {
        key = StringUtils.trimToEmpty(key).toLowerCase();
        if (!clauses.containsKey(key)) {
            throw new IllegalArgumentException("Key '"+key+"' is not defined");
        }
        LinkedHashMap<String, Object> newMap = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : clauses.entrySet()) {
            if (entry.getKey().equals(key)) {
                newMap.put(key, newValue);
            } else {
                newMap.put(entry.getKey(), entry.getValue());
            }
        }
        this.clauses = newMap;

        return this;
    }

    /**
     * Inserts a new clause before the given key. Throws IllegalArgumentException if the existingKey does not exist.
     */
    public StringClauses insertBefore(String existingKey, String newKey, String newValue) throws IllegalArgumentException {
        return insertBeforeImpl(existingKey, newKey, newValue);
    }

    /**
     * Inserts a new sub-clause before the given key. Throws IllegalArgumentException if the existingKey does not exist.
     */
    public StringClauses insertBefore(String existingKey, String newKey, StringClauses newValue) {
        return insertBeforeImpl(existingKey, newKey, newValue);
    }

    /**
     * Convenience method for {@link #insertBefore(String, String, String)} where the new clause key is equal to the newValue.
     */
    public StringClauses insertBefore(String existingKey, String newValue) {
        return insertBefore(existingKey, newValue, StringUtils.trimToNull(newValue));
    }

    /**
     * Convenience method for {@link #insertBefore(String, String)} when using enums.
     */
    public StringClauses insertBefore(Enum existingKey, String newValue) {
        return insertBefore(existingKey.name(), newValue);
    }

    /**
     * Convenience method for {@link #insertBefore(String, String)} when using enums.
     */
    public StringClauses insertBefore(Enum existingKey, Enum newKey, String newValue) {
        return insertBefore(existingKey.name(), newKey.name(), newValue);
    }


    private StringClauses insertBeforeImpl(String existingKey, String newKey, Object newValue) {
        existingKey = StringUtils.trimToEmpty(existingKey).toLowerCase();
        newKey = StringUtils.trimToEmpty(newKey).toLowerCase();

        if (!clauses.containsKey(existingKey)) {
            throw new IllegalArgumentException("Existing key '"+existingKey+"' does not exist");
        }
        if (newKey.equals("")) {
            throw new IllegalArgumentException("New key cannot be null or empty");
        }

        LinkedHashMap<String, Object> newMap = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : clauses.entrySet()) {
            if (entry.getKey().equals(existingKey)) {
                newMap.put(newKey, newValue);
            }
            newMap.put(entry.getKey(), entry.getValue());
        }
        this.clauses = newMap;

        return this;
    }

    /**
     * Inserts a new clause after the given key. Throws IllegalArgumentException if the existingKey does not exist.
     */
    public StringClauses insertAfter(String existingKey, String newKey, String newValue) {
        return insertAfterImpl(existingKey, newKey, newValue);
    }

    /**
     * Inserts a new sub-clause after the given key. Throws IllegalArgumentException if the existingKey does not exist.
     */
    public StringClauses insertAfter(String existingKey, String newKey, StringClauses newValue) {
        return insertAfterImpl(existingKey, newKey, newValue);
    }

    /**
     * Convenience method for {@link #insertAfter(String, String)} when using enums
     */
    public StringClauses insertAfter(Enum existingKey, String newValue) {
        return insertAfter(existingKey.name(), newValue);
    }

    /**
     * Convenience method for {@link #insertAfter(String, String, String)} using the newValue as the newKey.
     */
    public StringClauses insertAfter(String existingKey, String newValue) {
        return insertAfter(existingKey, newValue, StringUtils.trimToEmpty(newValue));
    }

    private StringClauses insertAfterImpl(String existingKey, String newKey, Object newValue) {
        existingKey = StringUtils.trimToEmpty(existingKey).toLowerCase();
        newKey = StringUtils.trimToEmpty(newKey).toLowerCase();

        if (!clauses.containsKey(existingKey)) {
            throw new IllegalArgumentException("Existing key '"+existingKey+"' does not exist");
        }
        if (newKey.equals("")) {
            throw new IllegalArgumentException("New key cannot be null or empty");
        }

        LinkedHashMap<String, Object> newMap = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : clauses.entrySet()) {
            newMap.put(entry.getKey(), entry.getValue());
            if (entry.getKey().equals(existingKey)) {
                newMap.put(newKey, newValue);
            }
        }
        this.clauses = newMap;

        return this;
    }

    /**
     * Retrieves the given key. Returns null if not set.
     * If clause at key is a StringClause, return the string version of it.
     * Will traverse sub-clauses to find the key.
     */
    public String get(String exitingKey) {
        exitingKey = StringUtils.trimToEmpty(exitingKey).toLowerCase();
        Object clauses = getImpl(exitingKey);
        if (clauses == null) {
            return null;
        }
        return clauses.toString();
    }

    private Object getImpl(String exitingKey) {
        Object o = this.clauses.get(exitingKey);
        if (o == null) {
            for (Object obj : this.clauses.values()) {
                if (obj instanceof StringClauses) {
                    Object childObj = ((StringClauses) obj).getImpl(exitingKey);
                    if (childObj != null) {
                        return childObj;
                    }
                }
            }
        }
        return o;
    }

    /**
     * Convenience method for {@link #get(String)} when using enums.
     */
    public String get(Enum exitingKey) {
        return get(exitingKey.name());
    }

    /**
     * Retrieves the given key. Returns null if not set. If clause at key is a String, return a new StringClause version of it. Will traverse sub-clauses to find the key.
     */
    public StringClauses getSubclause(String exitingKey) {
        exitingKey = StringUtils.trimToEmpty(exitingKey).toLowerCase();
        Object clauses = getImpl(exitingKey);
        if (clauses == null) {
            return null;
        }
        if (clauses instanceof String) {
            return new StringClauses().append((String) clauses);
        }
        return (StringClauses) clauses;
    }

    /**
     * Convenience method for {@link #getSubclause(String)} when using enums
     */
    public StringClauses getSubclause(Enum exitingKey) {
        return getSubclause(exitingKey.name());
    }

    @Override
    public String toString() {
        if (clauses.size() == 0) {
            return "";
        }

        List finalList = new ArrayList<>(clauses.values());
        ListIterator iterator = finalList.listIterator();
        while (iterator.hasNext()) {
            Object next = iterator.next();
            if (next == null || next.toString().equals("")) {
                iterator.remove();
            }
        }

        return start
                + StringUtils.join(finalList, separator, new StringUtils.ToStringFormatter())
                + end;
    }
}
