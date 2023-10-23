package liquibase.extension.testing;

import liquibase.Scope;
import liquibase.configuration.AbstractMapConfigurationValueProvider;
import liquibase.parser.core.yaml.YamlParser;
import liquibase.util.CollectionUtil;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class LiquibaseSdkConfigurationValueProvider extends AbstractMapConfigurationValueProvider {

    private final Map<String, Object> properties;

    public LiquibaseSdkConfigurationValueProvider() {
        properties = new HashMap<>();
        Yaml yaml = new Yaml(new SafeConstructor(YamlParser.createLoaderOptions()));
        try {
            final ArrayList<URL> urls = new ArrayList<>();
            urls.addAll(Collections.list(this.getClass().getClassLoader().getResources("liquibase.sdk.yaml")));
            urls.addAll(Collections.list(this.getClass().getClassLoader().getResources("liquibase.sdk.yml")));
            urls.addAll(Collections.list(this.getClass().getClassLoader().getResources("liquibase.sdk.local.yaml")));
            urls.addAll(Collections.list(this.getClass().getClassLoader().getResources("liquibase.sdk.local.yml")));

            for (URL url : urls) {
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
        return 40;
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
    protected boolean isValueSet(Object value) {
        return value != null;
    }
}
