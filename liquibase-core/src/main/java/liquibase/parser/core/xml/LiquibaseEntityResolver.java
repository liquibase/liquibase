package liquibase.parser.core.xml;

import liquibase.Scope;
import liquibase.logging.Logger;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.resource.InputStreamList;
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

    @Override
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

        //need to ensure XSD can be loaded from the system classpath, even if resourceAccessor is not configured to look there
        InputStreamList streams = new CompositeResourceAccessor(Scope.getCurrentScope().getResourceAccessor(), new ClassLoaderResourceAccessor(getClass().getClassLoader())).openStreams(null, path);
        if (streams.isEmpty()) {
            log.fine("Unable to resolve XML entity locally. Will load from network.");
            return null;
        } else if (streams.size() == 1) {
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

    @Override
    public InputSource getExternalSubset(String name, String baseURI) throws SAXException, IOException {
        return null;
    }

    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        Scope.getCurrentScope().getLog(getClass()).warning("Current XML parsers seems to not support EntityResolver2. External entities won't be correctly loaded");
        return resolveEntity(null, publicId, null, systemId);
    }
}
