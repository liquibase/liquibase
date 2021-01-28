package liquibase.configuration.core;

import liquibase.configuration.ConfigurationValueProvider;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Searches for the configuration values in the system environment variables.
 *
 * To handle shells that only allow underscores, it checks the following variations of a proprty:
 * <ul>
 * <li>foo.bar - the original name</li>
 * <li>foo_bar - with underscores for periods (if any)</li>
 * <li>FOO.BAR - original, with upper case</li>
 * <li>FOO_BAR - with underscores and upper case</li>
 * </ul>
 * Any hyphen variant of the above would work as well, or even mix dot/hyphen variants.
 *
 */
public class EnvironmentVariableProvider implements ConfigurationValueProvider {

    @Override
    public int getPrecedence() {
        return 10;
    }

    @Override
    public Object getValue(String property) {
        if (property == null) {
            return null;
        }

        Set<String> checked = new HashSet<>();

        for (String name : new String[]{property, property.toUpperCase(), property.toLowerCase()}) {
            for (String variation : new String[] {
                    name,

                    // Check name with just dots replaced
                    name.replace('.', '_'),

                    // Check name with just hyphens replaced
                    name.replace('-', '_'),

                    // Check name with dots and hyphens replaced
                    name.replace('.', '_').replace('-', '_')
            }) {
                if (checked.add(variation)) {
                    final String foundValue = getEnvironmentVariable(variation);
                    if (foundValue != null) {
                        return foundValue;
                    }

                }

            }
        }

        return null;
    }

    protected String getEnvironmentVariable(String name) {
        return System.getenv(name);
    }

    @Override
    public String describeValueLookupLogic(String property) {
        return "Environment variable '" + property + "'";
    }
}
