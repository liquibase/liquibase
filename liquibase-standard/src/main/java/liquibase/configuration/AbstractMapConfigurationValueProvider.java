package liquibase.configuration;

import liquibase.util.StringUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Convenience class for {@link ConfigurationValueProvider}s that can collect the possible values into a Map.
 * By default, it will follow standardized fuzzy-matching rules including being case insensitive, checking camelCase and kabob-case, etc.
 *
 * @see #keyMatches(String, String)
 */
public abstract class AbstractMapConfigurationValueProvider extends AbstractConfigurationValueProvider {

    private final Map<String, ProvidedValue> knownValues = new HashMap<>();
    private int knownValuesForHash = 0;
    private final Lock knownValuesLock = new ReentrantLock();

    protected abstract Map<?, ?> getMap();

    protected abstract String getSourceDescription();

    /**
     * Default implementation stores a cache of found known values, falling back to {@link #lookupProvidedValue(String...)} when it is asked about a new key.
     * Uses {@link #getMapHash()} to determine if the underlying map has changed.
     */
    @Override
    public ProvidedValue getProvidedValue(String... keyAndAliases) {
        final Lock lock = this.knownValuesLock;
        lock.lock();
        try {
            int currentValuesHash = getMapHash();
            if (currentValuesHash == knownValuesForHash) {
                for (String key : keyAndAliases) {
                    if (knownValues.containsKey(key)) {
                        return knownValues.get(key);
                    }
                }
            } else {
                knownValues.clear();
                knownValuesForHash = currentValuesHash;
            }
        } finally {
            lock.unlock();
        }

        final ProvidedValue providedValue = lookupProvidedValue(keyAndAliases);
        if (providedValue == null) {
            for (String key : keyAndAliases) {
                knownValues.put(key, null);
            }
        } else {
            knownValues.put(providedValue.getRequestedKey(), providedValue);
            knownValues.put(providedValue.getActualKey(), providedValue);
        }

        return providedValue;
    }

    /**
     * Used by {@link #getProvidedValue(String...)} to determine if the underlying data has changed vs. the cached results.
     */
    protected int getMapHash() {
        final Map<?, ?> map = getMap();
        if (map == null) {
            return -1;
        }
        return map.hashCode();
    }


    /**
     * Finds the given key in the result of {@link #getMap()} using {@link #keyMatches(String, String)} to determine key equality.
     * Subclasses should usually override this method rather than {@link #getProvidedValue(String...)} so the caching functionality is not lost.
     */
    protected ProvidedValue lookupProvidedValue(String... keyAndAliases) {
        if (keyAndAliases == null || keyAndAliases.length == 0) {
            return null;
        }

        final Map<?, ?> sourceData = getMap();

        if (sourceData == null) {
            return null;
        }

        for (String key : keyAndAliases) {
            //try direct lookup first, for performance:
            if (sourceData.containsKey(key)) {
                final Object foundValue = sourceData.get(key);
                if (isValueSet(foundValue)) {
                    return new ProvidedValue(keyAndAliases[0], key, foundValue, getSourceDescription(), this);
                }
            }


            for (Map.Entry<?, ?> entry : sourceData.entrySet()) {
                final String actualKey = String.valueOf(entry.getKey());

                if (keyMatches(key, actualKey)) {
                    final Object value = entry.getValue();
                    if (isValueSet(value)) {
                        return new ProvidedValue(keyAndAliases[0], actualKey, value, getSourceDescription(), this);
                    }
                }
            }
        }

        return null;
    }

    /**
     * Used by {@link ConfigurationValueProvider#getProvidedValue(String[])} to determine if the given value is a "real" value.
     * This implementation returns false if value is null or if it is an empty string
     */
    protected boolean isValueSet(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof String) {
            return StringUtil.isNotEmpty((String) value);
        }

        return true;
    }

    /**
     * Used by {@link ConfigurationValueProvider#getProvidedValue(String[])} to determine of a given map entry matches the wanted key.
     * This implementation compares the values case-insensitively, and will replace camelCase words with kabob-case
     *
     * @param wantedKey the configuration key requested
     * @param storedKey the key stored in the map
     */
    protected boolean keyMatches(String wantedKey, String storedKey) {
        if (wantedKey.equalsIgnoreCase(storedKey)) {
            return true;
        }

        wantedKey = StringUtil.toKabobCase(wantedKey);
        if (wantedKey.equalsIgnoreCase(storedKey)) {
            return true;
        }

        wantedKey = wantedKey.replace(".", "-");
        if (wantedKey.equalsIgnoreCase(storedKey)) {
            return true;
        }

        //check for everythingSmashedTogether case insensitively
        wantedKey = wantedKey.replace("-", "");
        return wantedKey.equalsIgnoreCase(storedKey);
    }

}
