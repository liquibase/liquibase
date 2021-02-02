package liquibase.configuration.core;

import liquibase.configuration.ConfigurationValueProvider;
import liquibase.configuration.CurrentValueDetails;
import liquibase.configuration.CurrentValueSourceDetails;

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
 */
public class SystemEnvironmentValueProvider implements ConfigurationValueProvider {

    @Override
    public int getPrecedence() {
        return 10;
    }

    @Override
    public CurrentValueSourceDetails getValue(String key) {
        if (key == null) {
            return null;
        }

        Set<String> checked = new HashSet<>();

        for (String name : new String[]{key, key.toUpperCase(), key.toLowerCase()}) {
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
                        return new CurrentValueSourceDetails(foundValue, "Environment variable", variation);
                    }

                }

            }
        }

        return null;
    }

    protected String getEnvironmentVariable(String name) {
        return System.getenv(name);
    }

}
