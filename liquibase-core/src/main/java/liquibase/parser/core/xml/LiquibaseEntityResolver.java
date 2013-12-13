package liquibase.parser.core.xml;

import liquibase.parser.LiquibaseParser;
import liquibase.parser.NamespaceDetails;
import liquibase.parser.NamespaceDetailsFactory;
import liquibase.serializer.LiquibaseSerializer;
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
 * Finds the Liquibase schema from the classpath rather than fetching it over the Internet.
 * Also resolve external entities using a resourceAccessor if it's provided
 */
public class LiquibaseEntityResolver implements EntityResolver2 {

    private LiquibaseParser parser;
    private LiquibaseSerializer serializer;
    private ResourceAccessor resourceAccessor;
    private String basePath;

    private Logger log=LogFactory.getLogger();

    public LiquibaseEntityResolver(LiquibaseSerializer serializer) {
        this.serializer = serializer;
    }

    public LiquibaseEntityResolver(LiquibaseParser parser) {
        this.parser = parser;
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

   @Override
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
            NamespaceDetails namespaceDetails;
            if (serializer != null) {
                namespaceDetails = NamespaceDetailsFactory.getInstance().getNamespaceDetails(serializer, systemId);
            } else {
                namespaceDetails = NamespaceDetailsFactory.getInstance().getNamespaceDetails(parser, systemId);
            }
            if (namespaceDetails == null) {
                return null;
            }
            String xsdFile = namespaceDetails.getLocalPath(systemId);
            try {
              System.err.println("Looking for " + xsdFile + " for " + systemId);
                InputStream resourceAsStream = resourceAccessor.getResourceAsStream(xsdFile);
                System.err.println("Tried " + resourceAccessor + " and got " + resourceAsStream);
                if (resourceAsStream == null) {
                    if (Thread.currentThread().getContextClassLoader() != null) {
                        resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(xsdFile);
                        System.err.println("Tried thread context classloader and got " + resourceAsStream);
                    }
                    if (resourceAsStream == null) {
                        resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(xsdFile);
                        System.err.println("Tried lb classloader and got " + resourceAsStream);
                    }
                }
                if (resourceAsStream == null) {
                    return null;
                }
                InputSource source = new InputSource(resourceAsStream);
                source.setPublicId(publicId);
                source.setSystemId(systemId);
                return source;
            } catch (Exception ex) {
                ex.printStackTrace();
                return null; // We don't have the schema, try the network
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

    @Override
    public InputSource getExternalSubset(String name, String baseURI) throws SAXException, IOException {
        return null;
    }

    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        log.warning("Current XML parsers seems to not support EntityResolver2. External entities won't be correctly loaded");
        return tryResolveLiquibaseSchema(systemId, publicId);
    }

}
