package liquibase.util;

import java.math.BigInteger;
import java.security.SecureRandom;
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
    private final Random random = new SecureRandom();
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
     * Returns the key if it is not empty and/or not already used. If it is already used, it generates a random value for the key.
     */
    private String uniqueKey(String key) {
        boolean generateOne = false;

        key = StringUtils.trimToNull(key);
        if (key == null) {
            generateOne = true;
        } else if (clauses.containsKey(key.toLowerCase())) {
            generateOne = true;
        }

        while (generateOne) {
            key = StringUtils.leftPad(new BigInteger(50, random).toString(32), 6).replace(" ", "0").substring(0,6);
            generateOne = clauses.containsKey(key);
        }
        return key;
    }

    /**
     * Adds a new clause at the end of the list with the given key.
     */
    public StringClauses append(String key, String clause) {
        Validate.notNull(StringUtils.trimToNull(key), "key must be a non-null, non-empty value");

        key = StringUtils.trimToEmpty(key).toLowerCase();
        clause = StringUtils.trimToEmpty(clause);

        if (clauses.containsKey(key)) {
            throw new IllegalArgumentException("Cannot add clause with key '" + key + "' because it is already defined");
        }
        clauses.put(key, clause.trim());
        return this;
    }

    /**
     * Adds a new sub-clause at the end of the list with the given key.
     */
    public StringClauses append(String key, StringClauses subclauses) {
        Validate.notNull(StringUtils.trimToNull(key), "key must be a non-null, non-empty value");

        key = StringUtils.trimToEmpty(key).toLowerCase();

        if (clauses.containsKey(key)) {
            throw new IllegalArgumentException("Cannot add clause with key '" + key + "' because it is already defined");
        }

        clauses.put(key, subclauses);
        return this;
    }

    /**
     * Adds a new clause at the end of the list. The key is equal to the passed clause.
     * There is no corresponding append with just a StringClauses because the subclause may not be fully created when appended and so the key may be an unexpected value.
     */
    public StringClauses append(String clause) {
        return this.append(uniqueKey(clause), clause);
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

    public StringClauses append(LiteralClause literal) {
        if (literal != null) {
            clauses.put(literal.getClass().getName().toLowerCase() + " #" + clauses.size(), literal);
        }
        return this;
    }

    public StringClauses prepend(LiteralClause literal) {
        if (literal != null) {
            LinkedHashMap<String, Object> newMap = new LinkedHashMap<>();
            newMap.put(uniqueKey(literal.getClass().getName().toLowerCase()), literal);
            newMap.putAll(this.clauses);
            this.clauses = newMap;
        }
        return this;
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
        if (StringUtils.trimToNull(clause) == null) {
            return prepend(new Whitespace(clause));
        }
        return prepend(uniqueKey(clause), clause);
    }

    /**
     * Convenience method for {@link #prepend(String, String)} when using enums
     */
    public StringClauses prepend(Enum key, String clause) {
        return prepend(key.name(), clause);
    }

    /**
     * Convenience method for {@link #prepend(String, liquibase.util.StringClauses)} when using enums
     */
    public StringClauses prepend(Enum key, StringClauses clause) {
        return prepend(key.name(), clause);
    }


    private StringClauses prependImpl(String key, Object clause) throws IllegalArgumentException {
        Validate.notNull(StringUtils.trimToNull(key), "key must be a non-null, non-empty value");

        key = StringUtils.trimToEmpty(key).toLowerCase();

        if (clauses.containsKey(key)) {
            throw new IllegalArgumentException("Cannot add clause with key '" + key + "' because it is already defined");
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
     * Replaces the given key with a new string. If the existing key does not exist, throws IllegalArgumentException
     */
    public StringClauses replaceIfExists(String key, String newValue) throws IllegalArgumentException {
        if (contains(key)) {
            return replaceImpl(key, StringUtils.trimToEmpty(newValue));
        } else {
            return this;
        }
    }

    public boolean contains(String key) {
        return clauses.containsKey(key.toLowerCase());
    }

    /**
     * Replaces the given key with a new sub-clause. If the existing key does not exist, throws IllegalArgumentException
     */
    public StringClauses replace(String key, StringClauses newValue) {
        return replaceImpl(key, newValue);
    }

    /**
     * Convenience method for {@link #replace(String, liquibase.util.StringClauses)} when using enums.
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
            throw new IllegalArgumentException("Key '" + key + "' is not defined");
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
        Validate.notNull(StringUtils.trimToNull(newKey), "key must be a non-null, non-empty value");

        existingKey = StringUtils.trimToEmpty(existingKey).toLowerCase();
        newKey = StringUtils.trimToEmpty(newKey).toLowerCase();

        if (!clauses.containsKey(existingKey)) {
            throw new IllegalArgumentException("Existing key '" + existingKey + "' does not exist");
        }
        if ("".equals(newKey)) {
            throw new IllegalArgumentException("New key cannot be null or empty");
        }

        if (clauses.containsKey(newKey)) {
            throw new IllegalArgumentException("Cannot add clause with key '" + newKey + "' because it is already defined");
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
        Validate.notNull(StringUtils.trimToNull(existingKey), "key must be a non-null, non-empty value");

        existingKey = StringUtils.trimToEmpty(existingKey).toLowerCase();
        newKey = StringUtils.trimToEmpty(newKey).toLowerCase();

        if (!clauses.containsKey(existingKey)) {
            throw new IllegalArgumentException("Existing key '" + existingKey + "' does not exist");
        }
        if (clauses.containsKey(newKey)) {
            throw new IllegalArgumentException("Cannot add clause with key '" + newKey + "' because it is already defined");
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

    public ClauseIterator getClauseIterator() {
        return new ClauseIterator(clauses);
    }

    @Override
    public String toString() {
        if (clauses.isEmpty()) {
            return "";
        }

        List finalList = new ArrayList<>(clauses.values());
        ListIterator iterator = finalList.listIterator();
        while (iterator.hasNext()) {
            Object next = iterator.next();
            if ((next == null) || "".equals(next.toString())) {
                iterator.remove();
            }
        }

        return start
                + StringUtils.join(finalList, separator, new StringUtils.ToStringFormatter())
                + end;
    }

    public Object[] toArray(boolean stringify) {
        Object[] returnArray = new Object[clauses.size()];
        ArrayList<Object> currentValues = new ArrayList<>(clauses.values());

        for (int i=0; i<currentValues.size(); i++) {
            if (stringify) {
                returnArray[i] = currentValues.get(i).toString();
            } else {
                returnArray[i] = currentValues.get(i);
            }
        }

        return returnArray;
    }

    public boolean isEmpty() {
        if (clauses.isEmpty()) {
            return true;
        }
        for (Object clause : clauses.values()) {
            if ((clause != null) && !"".equals(clause.toString().trim())) {
                return false;
            }
        }
        return true;
    }

    public interface LiteralClause {
        String toString();
    }

    public static class Delimiter implements LiteralClause{
        private String value;

        public Delimiter(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    public static class Whitespace implements LiteralClause{
        private String value;

        public Whitespace(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    public static class Comment implements LiteralClause {
        private String value;

        public Comment(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    public static class ClauseIterator implements Iterator {

        private final LinkedHashMap<String, Object> clauses;
        private ListIterator<String> keyIterator;

        public ClauseIterator(LinkedHashMap<String, Object> clauses) {
            this.keyIterator = new ArrayList<String>(clauses.keySet()).listIterator();
            this.clauses = clauses;
        }

        @Override
        public boolean hasNext() {
            return keyIterator.hasNext();
        }

        @Override
        public Object next() {
            return clauses.get(keyIterator.next());
        }

        public Object nextNonWhitespace() {
            Object next;
            while (hasNext()) {
                next = clauses.get(keyIterator.next());
                if (!(next instanceof Whitespace) && !(next instanceof Comment)) {
                    return next;
                }
            }
            return null;
        }

        @Override
        public void remove() {
            clauses.remove(keyIterator.previous());
            keyIterator.remove();
        }

        public void replace(Object newClause) {
            keyIterator.previous();
            String keyToReplace = keyIterator.next();

            clauses.put(keyToReplace, newClause);
        }
    }
}
