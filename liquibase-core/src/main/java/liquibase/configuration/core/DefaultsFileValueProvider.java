package liquibase.configuration.core;

import liquibase.Scope;
import liquibase.configuration.AbstractMapConfigurationValueProvider;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.servicelocator.LiquibaseService;
import liquibase.util.StringUtil;

import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

@LiquibaseService(skip = true)
public class DefaultsFileValueProvider extends AbstractMapConfigurationValueProvider {

    private Properties properties;
    private String path;

    public DefaultsFileValueProvider(String path) {
        this.path = path;

        try (final InputStream stream = Scope.getCurrentScope().getResourceAccessor().openStream(null, path)) {
            if (stream != null) {
                this.properties = new Properties();
                this.properties.load(stream);
            }
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    protected DefaultsFileValueProvider(Properties properties) {
        this.properties = properties;
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
        return "File " + path;
    }
}
