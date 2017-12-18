package liquibase.parser.core.xml;

import liquibase.logging.LogService;
import liquibase.logging.LogType;
import liquibase.logging.Logger;
import liquibase.parser.LiquibaseParser;
import liquibase.resource.ResourceAccessor;
import liquibase.serializer.LiquibaseSerializer;
import liquibase.util.StreamUtil;
import liquibase.util.file.FilenameUtils;
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

    private LiquibaseParser parser;
    private LiquibaseSerializer serializer;
    private ResourceAccessor resourceAccessor;
    private String basePath;

    private Logger log= LogService.getLog(getClass());

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
       log.debug(LogType.LOG, "Resolving XML entity name='" + name + "', publicId='" + publicId + "', baseURI='" + baseURI + "', systemId='" + systemId + "'");

       if(systemId == null){
           log.debug(LogType.LOG, "Unable to resolve XML entity locally. Will load from network.");
           return null;
       }

       InputSource resolved=null;
       if(systemId.toLowerCase().endsWith(".xsd")) {
           if (systemId.startsWith("http://www.liquibase.org/xml/ns/migrator/")) {
               systemId = systemId.replace("http://www.liquibase.org/xml/ns/migrator/", "http://www.liquibase.org/xml/ns/dbchangelog/");
           }
            resolved = tryResolveLiquibaseSchema(systemId, publicId);
       }

	   if((resolved == null) && (resourceAccessor != null) && (basePath != null)) {
            resolved =  tryResolveFromResourceAccessor(systemId);
       }

       if (resolved == null) {
            log.debug(LogType.LOG, "Unable to resolve XML entity locally. Will load from network.");
       }
       return resolved;
    }

    private InputSource tryResolveLiquibaseSchema(String systemId, String publicId) {
        LiquibaseSchemaResolver liquibaseSchemaResolver = new LiquibaseSchemaResolver(systemId, publicId, resourceAccessor);
        if (serializer != null) {
            return liquibaseSchemaResolver.resolve(serializer);
        } else {
            return liquibaseSchemaResolver.resolve(parser);
        }
    }

    private InputSource tryResolveFromResourceAccessor(String systemId) {
        String path=FilenameUtils.concat(basePath, systemId);
        log.debug(LogType.LOG, "Attempting to load "+systemId+" from resourceAccessor as "+path);

        try {
            InputStream resourceAsStream = StreamUtil.singleInputStream(path, resourceAccessor);
            if (resourceAsStream == null) {
                log.debug(LogType.LOG, "Could not load "+systemId+" from resourceAccessor as "+path);
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
        log.warning(LogType.LOG, "Current XML parsers seems to not support EntityResolver2. External entities won't be correctly loaded");
        return tryResolveLiquibaseSchema(systemId, publicId);
    }

}
