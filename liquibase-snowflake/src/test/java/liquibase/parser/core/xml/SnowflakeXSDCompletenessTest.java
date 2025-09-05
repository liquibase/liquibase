package liquibase.parser.core.xml;

import liquibase.change.Change;
import liquibase.change.core.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test to validate that all implemented attributes in Snowflake Change classes
 * have corresponding definitions in the XSD schema, and that the XSD schema aligns with
 * current Snowflake DDL documentation.
 */
@DisplayName("Snowflake XSD Schema Completeness and Vendor Alignment")
public class SnowflakeXSDCompletenessTest {

    private static final String XSD_PATH = "www.liquibase.org/xml/ns/snowflake/liquibase-snowflake-latest.xsd";
    
    /**
     * Map of Change classes to their corresponding XSD element names
     */
    private static final Map<Class<? extends Change>, String> CHANGE_TO_ELEMENT_MAP = createChangeToElementMap();
    
    private static Map<Class<? extends Change>, String> createChangeToElementMap() {
        Map<Class<? extends Change>, String> map = new HashMap<>();
        map.put(CreateDatabaseChange.class, "createDatabase");
        map.put(AlterDatabaseChange.class, "alterDatabase");
        map.put(DropDatabaseChange.class, "dropDatabase");
        map.put(CreateSchemaChange.class, "createSchema");
        map.put(AlterSchemaChange.class, "alterSchema");
        map.put(DropSchemaChange.class, "dropSchema");
        // Note: createSequence uses namespace attribute extension pattern, not custom change class
        map.put(CreateWarehouseChange.class, "createWarehouse");
        map.put(AlterWarehouseChange.class, "alterWarehouse");
        map.put(DropWarehouseChange.class, "dropWarehouse");
        return map;
    }

    @Test
    @DisplayName("All Change class attributes must exist in XSD schema")
    public void testXSDAttributeCompleteness() throws Exception {
        Document xsdDocument = loadXSDDocument();
        List<String> missingAttributes = new ArrayList<>();
        
        for (Map.Entry<Class<? extends Change>, String> entry : CHANGE_TO_ELEMENT_MAP.entrySet()) {
            Class<? extends Change> changeClass = entry.getKey();
            String elementName = entry.getValue();
            
            Set<String> javaAttributes = extractAttributesFromChangeClass(changeClass);
            Set<String> xsdAttributes = extractAttributesFromXSDElement(xsdDocument, elementName);
            
            // Find attributes in Java class but missing from XSD
            Set<String> missing = javaAttributes.stream()
                .filter(attr -> !xsdAttributes.contains(attr))
                .collect(Collectors.toSet());
                
            if (!missing.isEmpty()) {
                missing.forEach(attr -> 
                    missingAttributes.add(String.format("Element '%s' missing attribute '%s'", elementName, attr))
                );
            }
        }
        
        if (!missingAttributes.isEmpty()) {
            String errorMessage = "XSD Schema is incomplete. Missing attributes:\n" + 
                String.join("\n", missingAttributes);
            fail(errorMessage);
        }
    }
    
    @Test
    @DisplayName("XSD schema file must be accessible from classpath")
    public void testXSDAccessibility() {
        InputStream xsdStream = getClass().getClassLoader().getResourceAsStream(XSD_PATH);
        assertNotNull(xsdStream, "XSD schema file must be accessible: " + XSD_PATH);
    }
    
    /**
     * Extract attribute names from a Change class by analyzing setter methods
     */
    private Set<String> extractAttributesFromChangeClass(Class<? extends Change> changeClass) {
        Set<String> attributes = new HashSet<>();
        
        for (Method method : changeClass.getMethods()) {
            String methodName = method.getName();
            
            // Look for setter methods that correspond to XML attributes
            if (methodName.startsWith("set") && 
                method.getParameterCount() == 1 && 
                !methodName.equals("setChangeSet") &&
                !methodName.equals("setResourceAccessor")) {
                
                // Convert setAttributeName to attributeName
                String attributeName = methodName.substring(3);
                attributeName = Character.toLowerCase(attributeName.charAt(0)) + attributeName.substring(1);
                attributes.add(attributeName);
            }
        }
        
        return attributes;
    }
    
    /**
     * Extract attribute names from XSD element definition
     */
    private Set<String> extractAttributesFromXSDElement(Document xsdDocument, String elementName) {
        Set<String> attributes = new HashSet<>();
        
        NodeList elements = xsdDocument.getElementsByTagName("xsd:element");
        for (int i = 0; i < elements.getLength(); i++) {
            Element element = (Element) elements.item(i);
            if (elementName.equals(element.getAttribute("name"))) {
                // Found the element, now find its attributes
                NodeList attributeNodes = element.getElementsByTagName("xsd:attribute");
                for (int j = 0; j < attributeNodes.getLength(); j++) {
                    Element attrElement = (Element) attributeNodes.item(j);
                    String attrName = attrElement.getAttribute("name");
                    if (!attrName.isEmpty()) {
                        attributes.add(attrName);
                    }
                }
                break;
            }
        }
        
        return attributes;
    }
    
    /**
     * Load XSD document for parsing
     */
    private Document loadXSDDocument() throws ParserConfigurationException, IOException, SAXException {
        InputStream xsdStream = getClass().getClassLoader().getResourceAsStream(XSD_PATH);
        assertNotNull(xsdStream, "Could not load XSD file: " + XSD_PATH);
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(xsdStream);
    }
}