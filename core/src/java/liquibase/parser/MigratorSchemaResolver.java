package liquibase.parser;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.InputStream;

/**
 * Finds the LiquiBase schema from the classpath rather than fetching it over the Internet.
 */
public class MigratorSchemaResolver implements EntityResolver {

    private static final String SEARCH_PACKAGE = "liquibase/";

    public InputSource resolveEntity(String publicId, String systemId) throws IOException {
        if (systemId != null) {
            int iSlash = systemId.lastIndexOf('/');
            if (iSlash >= 0) {
                String xsdFile = systemId.substring(iSlash + 1);
                try {
                    InputStream resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(SEARCH_PACKAGE + xsdFile);

                    if (resourceAsStream == null) {
                       resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(SEARCH_PACKAGE + xsdFile);
                    }

                    if (resourceAsStream == null) {
                        return null;
                    }
                    InputSource source = new InputSource(resourceAsStream);
                    source.setPublicId(publicId);
                    source.setSystemId(systemId);
                    return source;
                }
                catch (Exception ex) {
                    return null;    // We don't have the schema, try the network
                }
            }
        }
        return null;
    }

}
