package liquibase.parser.core.xml;

import liquibase.parser.core.ParsedNode;
import liquibase.changelog.ChangeLogParameters;
import liquibase.resource.ResourceAccessor;
import liquibase.exception.ChangeLogParseException;
import liquibase.ext.snowflake.SnowflakeNamespaceAttributeStorage;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom XML parser that captures Snowflake namespace attributes.
 */
public class SnowflakeNamespaceAwareXMLChangeLogSAXParser extends XMLChangeLogSAXParser {
    
    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT + 10; // Higher priority than default
    }
    
    @Override
    protected ParsedNode parseToNode(String physicalChangeLogLocation, 
                                    ChangeLogParameters changeLogParameters, 
                                    ResourceAccessor resourceAccessor) 
                                    throws ChangeLogParseException {
        
        System.out.println("[SnowflakeNamespaceAwareXMLParser] Parsing " + physicalChangeLogLocation);
        
        // First, use SAX to capture namespace attributes
        try {
            captureNamespaceAttributes(physicalChangeLogLocation, resourceAccessor);
        } catch (Exception e) {
            System.err.println("[SnowflakeNamespaceAwareXMLParser] Failed to capture namespace attributes: " + e.getMessage());
            // Don't fail - continue with normal parsing
        }
        
        // Then do normal parsing
        return super.parseToNode(physicalChangeLogLocation, changeLogParameters, resourceAccessor);
    }
    
    private void captureNamespaceAttributes(String location, ResourceAccessor resourceAccessor) 
            throws Exception {
        
        try (InputStream inputStream = resourceAccessor.openStream(null, location)) {
            if (inputStream == null) {
                return;
            }
            
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
            
            // Handle createSequence elements
            if ("createSequence".equals(localName)) {
                String sequenceName = attributes.getValue("sequenceName");
                System.out.println("[SnowflakeNamespaceAwareXMLParser] Found createSequence: " + sequenceName);
                
                // Look for namespace attributes
                Map<String, String> namespaceAttrs = new HashMap<>();
                
                for (int i = 0; i < attributes.getLength(); i++) {
                    String attrUri = attributes.getURI(i);
                    String attrLocalName = attributes.getLocalName(i);
                    String attrValue = attributes.getValue(i);
                    String attrQName = attributes.getQName(i);
                    
                    System.out.println("[SnowflakeNamespaceAwareXMLParser]   Checking attribute: " + 
                                     attrQName + " (uri: " + attrUri + ", localName: " + attrLocalName + ")");
                    
                    if (SNOWFLAKE_NS.equals(attrUri) || (attrQName != null && attrQName.startsWith("snowflake:"))) {
                        System.out.println("[SnowflakeNamespaceAwareXMLParser]   Snowflake attribute found: " + 
                                         attrLocalName + " = " + attrValue);
                        namespaceAttrs.put(attrLocalName, attrValue);
                    }
                }
                
                if (!namespaceAttrs.isEmpty() && sequenceName != null) {
                    SnowflakeNamespaceAttributeStorage.storeAttributes("sequence", sequenceName, namespaceAttrs);
                }
            }
            
            // Handle createTable elements (for consistency with existing pattern)
            else if ("createTable".equals(localName)) {
                String tableName = attributes.getValue("tableName");
                System.out.println("[SnowflakeNamespaceAwareXMLParser] Found createTable: " + tableName);
                
                // Look for namespace attributes
                Map<String, String> namespaceAttrs = new HashMap<>();
                
                for (int i = 0; i < attributes.getLength(); i++) {
                    String attrUri = attributes.getURI(i);
                    String attrLocalName = attributes.getLocalName(i);
                    String attrValue = attributes.getValue(i);
                    String attrQName = attributes.getQName(i);
                    
                    System.out.println("[SnowflakeNamespaceAwareXMLParser]   Checking attribute: " + 
                                     attrQName + " (uri: " + attrUri + ", localName: " + attrLocalName + ")");
                    
                    if (SNOWFLAKE_NS.equals(attrUri) || (attrQName != null && attrQName.startsWith("snowflake:"))) {
                        System.out.println("[SnowflakeNamespaceAwareXMLParser]   Snowflake attribute found: " + 
                                         attrLocalName + " = " + attrValue);
                        namespaceAttrs.put(attrLocalName, attrValue);
                    }
                }
                
                if (!namespaceAttrs.isEmpty() && tableName != null) {
                    SnowflakeNamespaceAttributeStorage.storeAttributes("table", tableName, namespaceAttrs);
                }
            }
            // Handle createSchema elements
            else if ("createSchema".equals(localName)) {
                String schemaName = attributes.getValue("schemaName");
                System.out.println("[SnowflakeNamespaceAwareXMLParser] Found createSchema: " + schemaName);
                
                // Look for namespace attributes
                Map<String, String> namespaceAttrs = new HashMap<>();
                
                for (int i = 0; i < attributes.getLength(); i++) {
                    String attrUri = attributes.getURI(i);
                    String attrLocalName = attributes.getLocalName(i);
                    String attrValue = attributes.getValue(i);
                    String attrQName = attributes.getQName(i);
                    
                    System.out.println("[SnowflakeNamespaceAwareXMLParser]   Checking attribute: " + 
                                     attrQName + " (uri: " + attrUri + ", localName: " + attrLocalName + ")");
                    
                    if (SNOWFLAKE_NS.equals(attrUri) || (attrQName != null && attrQName.startsWith("snowflake:"))) {
                        System.out.println("[SnowflakeNamespaceAwareXMLParser]   Snowflake attribute found: " + 
                                         attrLocalName + " = " + attrValue);
                        namespaceAttrs.put(attrLocalName, attrValue);
                    }
                }
                
                if (!namespaceAttrs.isEmpty() && schemaName != null) {
                    SnowflakeNamespaceAttributeStorage.storeAttributes("schema", schemaName, namespaceAttrs);
                }
            }
        }
    }
}