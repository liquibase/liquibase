package liquibase.parser;

import liquibase.parser.core.xml.XMLChangeLogSAXParser;
import liquibase.parser.core.ParsedNode;
import liquibase.changelog.ChangeLogParameters;
import liquibase.resource.ResourceAccessor;
import liquibase.resource.Resource;
import liquibase.exception.ChangeLogParseException;
import liquibase.ext.SnowflakeNamespaceAttributeStorage;
import liquibase.Scope;
import liquibase.logging.Logger;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * XML parser that captures Snowflake namespace attributes.
 * Intercepts attributes with the snowflake: namespace prefix and stores them
 * for later use by SQL generators.
 */
public class SnowflakeNamespaceAwareXMLParser extends XMLChangeLogSAXParser {
    
    private static final Logger logger = Scope.getCurrentScope().getLog(SnowflakeNamespaceAwareXMLParser.class);
    
    @Override
    public int getPriority() {
        return PRIORITY_DATABASE + 10; // Higher priority than default
    }
    
    @Override
    protected ParsedNode parseToNode(String physicalChangeLogLocation, 
                                    ChangeLogParameters changeLogParameters, 
                                    ResourceAccessor resourceAccessor) 
                                    throws ChangeLogParseException {
        
        // First, capture namespace attributes
        try {
            captureNamespaceAttributes(physicalChangeLogLocation, resourceAccessor);
        } catch (Exception e) {
            // Log but don't fail - continue with normal parsing
        }
        
        // Then do normal parsing
        return super.parseToNode(physicalChangeLogLocation, changeLogParameters, resourceAccessor);
    }
    
    private void captureNamespaceAttributes(String location, ResourceAccessor resourceAccessor) 
            throws Exception {
        logger.fine("[SnowflakeNamespaceAwareXMLParser] Parsing " + location);
        
        List<Resource> resources = resourceAccessor.getAll(location);
        if (resources == null || resources.isEmpty()) {
            return;
        }
        
        try (InputStream inputStream = resources.get(0).openInputStream()) {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            SAXParser parser = factory.newSAXParser();
            
            NamespaceCapturingHandler handler = new NamespaceCapturingHandler();
            parser.parse(inputStream, handler);
        }
    }
    
    private static class NamespaceCapturingHandler extends DefaultHandler {
        private static final String SNOWFLAKE_NS = "http://www.liquibase.org/xml/ns/snowflake";
        
        @Override
        public void startElement(String uri, String localName, String qName, 
                                Attributes attributes) throws SAXException {
            
            // Check for the change types we're extending
            if (isTargetChangeType(localName)) {
                logger.fine("[SnowflakeNamespaceAwareXMLParser] Found " + localName + ": " + getObjectName(localName, attributes));
                String objectName = getObjectName(localName, attributes);
                
                // Look for namespace attributes
                Map<String, String> namespaceAttrs = new HashMap<>();
                
                for (int i = 0; i < attributes.getLength(); i++) {
                    logger.fine("[SnowflakeNamespaceAwareXMLParser]   Checking attribute: " + attributes.getLocalName(i) + " (uri: " + attributes.getURI(i) + ", localName: " + attributes.getLocalName(i) + ")");
                    if (SNOWFLAKE_NS.equals(attributes.getURI(i))) {
                        logger.fine("[SnowflakeNamespaceAwareXMLParser]   FOUND snowflake namespace attribute: " + attributes.getLocalName(i) + " = " + attributes.getValue(i));
                        namespaceAttrs.put(attributes.getLocalName(i), 
                                         attributes.getValue(i));
                    }
                }
                
                if (!namespaceAttrs.isEmpty() && objectName != null) {
                    logger.fine("DEBUG: Storing namespace attributes for " + objectName + ": " + namespaceAttrs);
                    SnowflakeNamespaceAttributeStorage.storeAttributes(objectName, namespaceAttrs);
                }
            }
        }
        
        private boolean isTargetChangeType(String localName) {
            // Add all change types we're extending
            return "createTable".equals(localName) ||
                   "alterTable".equals(localName) ||
                   "dropTable".equals(localName) ||
                   "renameTable".equals(localName) ||
                   "createSequence".equals(localName) ||
                   "alterSequence".equals(localName) ||
                   "dropSequence".equals(localName) ||
                   "createFileFormat".equals(localName) ||
                   "alterFileFormat".equals(localName) ||
                   "dropFileFormat".equals(localName);
        }
        
        private String getObjectName(String changeType, Attributes attributes) {
            // Extract the object name based on change type
            switch (changeType) {
                case "createTable":
                case "alterTable":
                case "dropTable":
                    return attributes.getValue("tableName");
                case "renameTable":
                    return attributes.getValue("oldTableName");
                case "createSequence":
                case "alterSequence":
                case "dropSequence":
                    return attributes.getValue("sequenceName");
                case "createFileFormat":
                case "alterFileFormat":
                case "dropFileFormat":
                    return attributes.getValue("fileFormatName");
                default:
                    return null;
            }
        }
    }
}