package liquibase.parser.core.xml;

import liquibase.parser.LiquibaseParser;
import liquibase.parser.NamespaceDetails;
import liquibase.parser.NamespaceDetailsFactory;
import liquibase.serializer.LiquibaseSerializer;
import liquibase.util.StreamUtil;
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
       log.debug("Resolving XML entity name='" + name + "', publicId='" + publicId + "', baseURI='" + baseURI + "', systemId='" + systemId + "'");
       InputSource resolved=null;
       if(systemId!=null && systemId.toLowerCase().endsWith(".xsd")) {
           if (systemId.startsWith("http://www.liquibase.org/xml/ns/migrator/")) {
               systemId = systemId.replace("http://www.liquibase.org/xml/ns/migrator/", "http://www.liquibase.org/xml/ns/dbchangelog/");
           }
            resolved=tryResolveLiquibaseSchema(systemId, publicId);
       }

       if (resolved == null) {
           log.debug("Unable to resolve XML entity locally. Will load from network.");
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
                log.debug("Found no namespace details class "+namespaceDetails.getClass().getName()+" for "+systemId);
                return null;
            }
            log.debug("Found namespace details class "+namespaceDetails.getClass().getName()+" for "+systemId);
            String xsdFile = namespaceDetails.getLocalPath(systemId);
            log.debug("Local path for "+systemId+" is "+xsdFile);

            if (xsdFile == null) {
                return null;
            }
            try {
                InputStream resourceAsStream = StreamUtil.singleInputStream(xsdFile, resourceAccessor);

                if (resourceAsStream == null) {
                    log.debug("Could not load "+xsdFile+" with the standard resource accessor. Trying context classloader...");
                    if (Thread.currentThread().getContextClassLoader() != null) {
                        resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(xsdFile);
                    }
                    if (resourceAsStream == null) {
                        log.debug("Could not load "+xsdFile+" with the standard resource accessor. Trying class classloader...");
                        resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(xsdFile);
                    }
                }
                if (resourceAsStream == null) {
                    log.debug("Could not find "+xsdFile+" locally");
                    return null;
                }

                log.debug("Successfully loaded XSD from "+xsdFile);
                InputSource source = new InputSource(resourceAsStream);
                source.setPublicId(publicId);
                source.setSystemId(systemId);
                return source;
            } catch (Exception ex) {
                return null; // We don't have the schema, try the network
            }
        }
        return null;
    }

    private InputSource tryResolveFromResourceAccessor(String systemId) {
        String path=FilenameUtils.concat(basePath, systemId);
        log.debug("Attempting to load "+systemId+" from resourceAccessor as "+path);

        try {
            InputStream resourceAsStream = StreamUtil.singleInputStream(path, resourceAccessor);
            if (resourceAsStream == null) {
                log.debug("Could not load "+systemId+" from resourceAccessor as "+path);
                return null;
            }
            return new InputSource(resourceAsStream);
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
