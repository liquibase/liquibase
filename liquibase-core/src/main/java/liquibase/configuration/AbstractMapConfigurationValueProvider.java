package liquibase.configuration;

import liquibase.util.StringUtil;

import java.util.Map;

/**
 * Convenience class for {@link ConfigurationValueProvider}s that can collect the possible values into a Map.
 * By default, it will follow standardized fuzzy-matching rules including being case insensitive, checking camelCase and kabob-case, etc.
 *
 * @see #keyMatches(String, String)
 */
public abstract class AbstractMapConfigurationValueProvider extends AbstractConfigurationValueProvider {

    protected abstract Map<?, ?> getMap();

    protected abstract String getSourceDescription();

    /**
     * Finds the given key in the result of {@link #getMap()} using {@link #keyMatches(String, String)} to determine key equality
     */
    @Override
    public ProvidedValue getProvidedValue(String... keyAndAliases) {
        if (keyAndAliases == null || keyAndAliases.length == 0) {
            return null;
        }

        final Map<?, ?> sourceData = getMap();

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
     * @param wantedKey the configuration key requested
     * @param storedKey the key stored in the map
     */
    protected boolean keyMatches(String wantedKey, String storedKey) {
        if (storedKey.equalsIgnoreCase(wantedKey)) {
            return true;
        }

        wantedKey = StringUtil.toKabobCase(wantedKey);
        if (storedKey.equalsIgnoreCase(wantedKey)) {
            return true;
        }

        return false;
    }

}
