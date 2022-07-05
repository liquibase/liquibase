package liquibase.parser.core.xml;

import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.logging.Logger;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.InputStreamList;
import liquibase.resource.ResourceAccessor;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.ext.EntityResolver2;

import java.io.IOException;
import java.io.InputStream;

/**
 * Finds the Liquibase schema from the classpath rather than fetching it over the Internet.
 * Also resolve external entities using a resourceAccessor if it's provided
 */
public class LiquibaseEntityResolver implements EntityResolver2 {

    private static ClassLoaderResourceAccessor fallbackResourceAccessor;

    @Override
    @java.lang.SuppressWarnings("squid:S2095")
    public InputSource resolveEntity(String name, String publicId, String baseURI, String systemId) throws SAXException, IOException {
        Logger log = Scope.getCurrentScope().getLog(getClass());

        log.fine("Resolving XML entity name='" + name + "', publicId='" + publicId + "', baseURI='" + baseURI + "', systemId='" + systemId + "'");

        if (systemId == null) {
            log.fine("Cannot determine systemId for name=" + name + ", publicId=" + publicId + ". Will load from network.");
            return null;
        }

        String path = systemId.toLowerCase()
                .replace("http://www.liquibase.org/xml/ns/migrator/", "http://www.liquibase.org/xml/ns/dbchangelog/")
                .replaceFirst("https?://", "");

        ResourceAccessor resourceAccessor = Scope.getCurrentScope().getResourceAccessor();
        InputStreamList streams = resourceAccessor.openStreams(null, path);
        if (streams.isEmpty()) {
            streams = getFallbackResourceAccessor().openStreams(null, path);

            if (streams.isEmpty()) {
                if (GlobalConfiguration.SECURE_PARSING.getCurrentValue()) {
                    String errorMessage = "Unable to resolve xml entity " + systemId + " locally: " +
                            GlobalConfiguration.SECURE_PARSING.getKey() + " is set to 'true' which does not allow remote lookups. " +
                            "Set it to 'false' to allow remote lookups of xsd files.";
                    throw new XSDLookUpException(errorMessage);
                } else {
                    log.fine("Unable to resolve XML entity locally. Will load from network.");
                    return null;
                }
            }
        }

        if (streams.size() == 1) {
            log.fine("Found XML entity at " + streams.getURIs().get(0));
        } else if (streams.size() > 1) {
            log.warning("Found " + streams.size() + " copies of " + systemId + ". Using " + streams.getURIs().get(0));
        }
        InputStream stream = streams.iterator().next();

        org.xml.sax.InputSource source = new org.xml.sax.InputSource(stream);
        source.setPublicId(publicId);
        source.setSystemId(systemId);

        return source;

    }

    /**
     * ResourceAccessor to use if the standard one does not have the XSD files in it.
     * Returns a ClassLoaderResourceAccessor that checks the system classloader which should include the liquibase.jar.
     */
    protected ResourceAccessor getFallbackResourceAccessor() {
        if (fallbackResourceAccessor == null) {
            fallbackResourceAccessor = new ClassLoaderResourceAccessor();
        }
        return fallbackResourceAccessor;
    }

    @Override
    public InputSource getExternalSubset(String name, String baseURI) throws SAXException, IOException {
        return null;
    }

    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        Scope.getCurrentScope().getLog(getClass()).warning("The current XML parser does not seems to not support EntityResolver2. External entities may not be correctly loaded");
        return resolveEntity(null, publicId, null, systemId);
    }
}
