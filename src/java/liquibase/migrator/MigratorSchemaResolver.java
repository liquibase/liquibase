package liquibase.migrator;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import java.io.IOException;

public class MigratorSchemaResolver implements EntityResolver {
    private static final String XSD_NAME = "dbchangelog-1.0.xsd";

    private static final String SEARCH_PACKAGE = "liquibase/";

    public InputSource resolveEntity(String publicId, String systemId) throws IOException {
        if (systemId != null && systemId.indexOf(XSD_NAME) > systemId.lastIndexOf("/")) {
            String xsdFile = systemId.substring(systemId.indexOf(XSD_NAME));
            try {
                InputSource source = new InputSource(getClass().getClassLoader().getResourceAsStream(SEARCH_PACKAGE + xsdFile));
                source.setPublicId(publicId);
                source.setSystemId(systemId);
                return source;
            }
            catch (Exception ex) {
                throw new IOException(ex.getMessage());
            }
        }
        return null;
    }

}
