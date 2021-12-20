package liquibase.extension.testing.environment.configuration;

import liquibase.Scope;
import liquibase.configuration.AbstractConfigurationValueProvider;
import liquibase.configuration.AbstractMapConfigurationValueProvider;
import liquibase.configuration.ProvidedValue;
import liquibase.util.CollectionUtil;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestEnvironmentConfigurationValueProvider extends AbstractMapConfigurationValueProvider {

    private final Map<String, Object> properties;

    public TestEnvironmentConfigurationValueProvider() {
        properties = new HashMap<>();
        Yaml yaml = new Yaml(new SafeConstructor());
        try {
            for (URL url : Collections.list(this.getClass().getClassLoader().getResources("liquibase.sdk.yaml"))) {
                try (InputStream stream = url.openStream()) {
                    Map settings = yaml.load(stream);

                    properties.putAll(CollectionUtil.flatten(settings));
                }

            }
        } catch (IOException e) {
            Scope.getCurrentScope().getLog(getClass()).info("Cannot find liquibase.test.yaml");
        }
    }

    @Override
    public int getPrecedence() {
        return 260;
    }

    @Override
    protected Map<?, ?> getMap() {
        return properties;
    }

    @Override
    protected String getSourceDescription() {
        return "liquibase.sdk.yaml file(s)";
    }

    @Override
    protected ProvidedValue lookupProvidedValue(String... keyAndAliases) {
        for (String key : keyAndAliases) {
            if (key.startsWith("liquibase.sdk.env.")) {
                String keySuffix = key.substring("liquibase.sdk.env.".length());
                String[] splitKey = keySuffix.split("\\.", 2);
                String envKey = splitKey[0];
                keySuffix = splitKey[1];

                final String dbKey = "liquibase.sdk.env." + envKey + "." + keySuffix;
                if (properties.containsKey(dbKey)) {
                    return super.lookupProvidedValue(dbKey);
                }

                //fall back to "default" setup
                ProvidedValue value = super.lookupProvidedValue("liquibase.sdk.env.default." + keySuffix);
                if (value != null) {
                    return value;
                }
            }
        }

        return null;
    }
}
