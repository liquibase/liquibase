package liquibase.configuration.core;

import liquibase.configuration.AbstractMapConfigurationValueProvider;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.servicelocator.LiquibaseService;
import liquibase.util.StringUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

@LiquibaseService(skip = true)
public class DefaultsFileValueProvider extends AbstractMapConfigurationValueProvider {

    private final Properties properties;
    private final String sourceDescription;

    public DefaultsFileValueProvider(File path) {
        this.sourceDescription = "File " + path.getAbsolutePath();

        try (InputStream stream = new FileInputStream(path)) {
            this.properties = new Properties();
            this.properties.load(stream);
            trimAllProperties();
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }

    }

    //
    // Remove trailing spaces on the property file values
    //
    private void trimAllProperties() {
        properties.forEach((key, value)  -> {
            if (value == null) {
                return;
            }
            if (! (value instanceof String)) {
                return;
            }
            properties.put(key, StringUtil.trimToEmpty((String) value));
        });
    }

    protected DefaultsFileValueProvider(Properties properties) {
        this.properties = properties;
        sourceDescription = "Passed default properties";
    }

    @Override
    public int getPrecedence() {
        return 50;
    }

    @Override
    protected Map<?, ?> getMap() {
        return properties;
    }

    @Override
    protected boolean keyMatches(String wantedKey, String storedKey) {
        if (super.keyMatches(wantedKey, storedKey)) {
            return true;
        }

        if (wantedKey.replaceFirst(".*\\.", "").equalsIgnoreCase(StringUtil.toCamelCase(storedKey))) {
            //Stored the argument name without a prefix
            return true;
        }

        return false;
    }

    @Override
    protected String getSourceDescription() {
        return sourceDescription;
    }
}
