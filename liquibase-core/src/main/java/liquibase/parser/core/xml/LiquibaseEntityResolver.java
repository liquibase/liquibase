package liquibase.parser.core.xml;

import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.InputStream;
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;
import liquibase.resource.ResourceAccessor;
import liquibase.util.file.FilenameUtils;
import org.xml.sax.SAXException;
import org.xml.sax.ext.EntityResolver2;

/**
 * Finds the LiquiBase schema from the classpath rather than fetching it over the Internet.
 * Also resolve external entities using a resourceAccessor if it's provided
 */
public class LiquibaseEntityResolver implements EntityResolver2 {

    private static final String SEARCH_PACKAGE = "liquibase/parser/core/xml/";

    private ResourceAccessor resourceAccessor;
    private String basePath;

    private Logger log=LogFactory.getLogger();

    public LiquibaseEntityResolver() {

    }

    /**
     * Use the resource accessor to resolve external entities
     * @param resourceAccessor Resource accessor to use
     * @param basePath Base path to use in the resourceAccessor
     */
    public void useResoureAccessor(ResourceAccessor resourceAccessor,String basePath) {
        this.resourceAccessor=resourceAccessor;
        this.basePath=basePath;
    }

   public InputSource resolveEntity(String name, String publicId, String baseURI, String systemId) throws SAXException, IOException {
       InputSource resolved=null;
       if(systemId!=null && systemId.toLowerCase().endsWith(".xsd")) {
            resolved=tryResolveLiquibaseSchema(systemId, publicId);
       }
       if(resolved==null && resourceAccessor!=null && basePath!=null && systemId!=null) {
            resolved=tryResolveFromResourceAccessor(systemId);
       }
       return resolved;
    }

    private InputSource tryResolveLiquibaseSchema(String systemId, String publicId) {
        if (systemId != null) {
            int iSlash = systemId.lastIndexOf('/');
            if (iSlash >= 0) {
                String xsdFile = systemId.substring(iSlash + 1);
                try {
                    InputStream resourceAsStream = null;
                    if (Thread.currentThread().getContextClassLoader() != null) {
                        resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(SEARCH_PACKAGE + xsdFile);
                    }
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
                } catch (Exception ex) {
                    return null; // We don't have the schema, try the network
                }
            }
        }
        return null;
    }

    private InputSource tryResolveFromResourceAccessor(String systemId) {
        String path=FilenameUtils.concat(basePath, systemId);
        try {
            return new InputSource(resourceAccessor.getResourceAsStream(path));
        }catch(Exception ex) {
            return null;
        }
    }

    public InputSource getExternalSubset(String name, String baseURI) throws SAXException, IOException {
        return null;
    }

    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        log.warning("Current XML parsers seems to not support EntityResolver2. External entities won't be correctly loaded");
        return tryResolveLiquibaseSchema(systemId, publicId);
    }

}
