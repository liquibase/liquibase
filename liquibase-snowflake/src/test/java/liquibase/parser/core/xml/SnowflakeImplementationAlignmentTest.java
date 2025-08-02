package liquibase.parser.core.xml;

import liquibase.change.Change;
import liquibase.change.core.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test-driven enforcement of Java implementation alignment with XSD schema.
 * This test validates that all Java Change classes have exactly the same attributes
 * as defined in the XSD schema - no more, no less.
 * 
 * ENFORCEMENT: This test MUST pass before any implementation is considered complete.
 * When this test fails, it provides exact Java code additions needed to achieve alignment.
 */
@DisplayName("Java Implementation vs XSD Schema Alignment")
public class SnowflakeImplementationAlignmentTest {

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
        map.put(CreateSequenceChangeSnowflake.class, "createSequence");
        map.put(CreateWarehouseChange.class, "createWarehouse");
        map.put(AlterWarehouseChange.class, "alterWarehouse");
        map.put(DropWarehouseChange.class, "dropWarehouse");
        map.put(AlterTableChange.class, "alterTable");
        return map;
    }
    
    @Test
    @DisplayName("CreateDatabaseChange Java implementation must exactly match XSD schema")
    public void testCreateDatabaseImplementationAlignment() {
        validateImplementationAlignment(CreateDatabaseChange.class, "createDatabase");
    }
    
    @Test
    @DisplayName("CreateWarehouseChange Java implementation must exactly match XSD schema")
    public void testCreateWarehouseImplementationAlignment() {
        validateImplementationAlignment(CreateWarehouseChange.class, "createWarehouse");
    }
    
    @Test
    @DisplayName("CreateSchemaChange Java implementation must exactly match XSD schema")
    public void testCreateSchemaImplementationAlignment() {
        validateImplementationAlignment(CreateSchemaChange.class, "createSchema");
    }
    
    @Test
    @DisplayName("AlterDatabaseChange Java implementation must exactly match XSD schema")
    public void testAlterDatabaseImplementationAlignment() {
        validateImplementationAlignment(AlterDatabaseChange.class, "alterDatabase");
    }
    
    @Test
    @DisplayName("AlterWarehouseChange Java implementation must exactly match XSD schema")
    public void testAlterWarehouseImplementationAlignment() {
        validateImplementationAlignment(AlterWarehouseChange.class, "alterWarehouse");
    }
    
    @Test
    @DisplayName("AlterSchemaChange Java implementation must exactly match XSD schema")
    public void testAlterSchemaImplementationAlignment() {
        validateImplementationAlignment(AlterSchemaChange.class, "alterSchema");
    }
    
    @Test
    @DisplayName("CreateSequenceChangeSnowflake Java implementation must exactly match XSD schema")
    public void testCreateSequenceImplementationAlignment() {
        validateImplementationAlignment(CreateSequenceChangeSnowflake.class, "createSequence");
    }
    
    @Test
    @DisplayName("DropDatabaseChange Java implementation must exactly match XSD schema")
    public void testDropDatabaseImplementationAlignment() {
        validateImplementationAlignment(DropDatabaseChange.class, "dropDatabase");
    }
    
    @Test
    @DisplayName("DropWarehouseChange Java implementation must exactly match XSD schema")
    public void testDropWarehouseImplementationAlignment() {
        validateImplementationAlignment(DropWarehouseChange.class, "dropWarehouse");
    }
    
    @Test
    @DisplayName("DropSchemaChange Java implementation must exactly match XSD schema")
    public void testDropSchemaImplementationAlignment() {
        validateImplementationAlignment(DropSchemaChange.class, "dropSchema");
    }
    
    @Test
    @DisplayName("AlterTableChange Java implementation must exactly match XSD schema")
    public void testAlterTableImplementationAlignment() {
        validateImplementationAlignment(AlterTableChange.class, "alterTable");
    }
    
    @Test
    @DisplayName("ALL Java Change classes must align with their XSD schema definitions")
    public void testAllImplementationsAlignment() {
        List<String> failures = new ArrayList<>();
        
        for (Map.Entry<Class<? extends Change>, String> entry : CHANGE_TO_ELEMENT_MAP.entrySet()) {
            try {
                validateImplementationAlignment(entry.getKey(), entry.getValue());
            } catch (AssertionError e) {
                failures.add(entry.getKey().getSimpleName() + ": " + e.getMessage());
            }
        }
        
        if (!failures.isEmpty()) {
            fail("Multiple implementation alignment failures:\n" + 
                 String.join("\n", failures));
        }
        
        System.out.printf("✅ ALL implementations aligned: %d Change classes validated%n", 
            CHANGE_TO_ELEMENT_MAP.size());
    }
    
    /**
     * Core validation method that enforces Java implementation alignment with XSD
     */
    private void validateImplementationAlignment(Class<? extends Change> changeClass, String elementName) {
        try {
            // Extract attributes from Java class
            Set<String> javaAttributes = extractJavaAttributes(changeClass);
            
            // Extract attributes from XSD schema
            Set<String> xsdAttributes = extractXSDAttributes(elementName);
            
            // Find missing attributes in Java implementation
            Set<String> missingInJava = xsdAttributes.stream()
                .filter(attr -> !javaAttributes.contains(attr))
                .collect(Collectors.toSet());
                
            // Find extra attributes in Java implementation
            Set<String> extraInJava = javaAttributes.stream()
                .filter(attr -> !xsdAttributes.contains(attr))
                .collect(Collectors.toSet());
            
            // Generate implementation commands if there are misalignments
            if (!missingInJava.isEmpty() || !extraInJava.isEmpty()) {
                String commands = generateJavaAlignmentCommands(
                    changeClass.getSimpleName(), missingInJava, extraInJava);
                
                fail(String.format(
                    "IMPLEMENTATION MISALIGNMENT: %s does not match XSD schema\n" +
                    "Missing in Java: %s\n" +
                    "Extra in Java: %s\n\n" +
                    "REQUIRED ACTIONS (copy-paste these):\n%s\n\n" +
                    "After making these changes, re-run this test to verify alignment.",
                    changeClass.getSimpleName(), missingInJava, extraInJava, commands
                ));
            }
            
            System.out.printf("✅ %s: Implementation aligned (%d attributes validated)%n", 
                changeClass.getSimpleName(), xsdAttributes.size());
                
        } catch (Exception e) {
            fail("Failed to validate " + changeClass.getSimpleName() + ": " + e.getMessage(), e);
        }
    }
    
    /**
     * Extracts attribute names from Java Change class by analyzing setter methods
     */
    private Set<String> extractJavaAttributes(Class<? extends Change> changeClass) {
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
     * Extracts attribute names from XSD element definition
     */
    private Set<String> extractXSDAttributes(String elementName) throws Exception {
        Set<String> attributes = new HashSet<>();
        
        InputStream xsdStream = getClass().getClassLoader().getResourceAsStream(XSD_PATH);
        assertNotNull(xsdStream, "Could not load XSD file: " + XSD_PATH);
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(xsdStream);
        
        NodeList elements = document.getElementsByTagName("xsd:element");
        for (int i = 0; i < elements.getLength(); i++) {
            Element element = (Element) elements.item(i);
            if (elementName.equals(element.getAttribute("name"))) {
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
     * Generates exact Java implementation commands for missing/extra attributes
     */
    private String generateJavaAlignmentCommands(String className, Set<String> missingInJava, Set<String> extraInJava) {
        StringBuilder commands = new StringBuilder();
        
        if (!missingInJava.isEmpty()) {
            commands.append("1. ADD to ").append(className).append(".java:\n");
            
            for (String attr : missingInJava) {
                String capitalizedAttr = attr.substring(0, 1).toUpperCase() + attr.substring(1);
                
                commands.append("   // Add field:\n");
                commands.append("   private String ").append(attr).append(";\n\n");
                
                commands.append("   // Add getter:\n");
                commands.append("   @DatabaseChangeProperty(description = \"Description for ").append(attr).append("\")\n");
                commands.append("   public String get").append(capitalizedAttr).append("() {\n");
                commands.append("       return ").append(attr).append(";\n");
                commands.append("   }\n\n");
                
                commands.append("   // Add setter:\n");
                commands.append("   public void set").append(capitalizedAttr).append("(String ").append(attr).append(") {\n");
                commands.append("       this.").append(attr).append(" = ").append(attr).append(";\n");
                commands.append("   }\n\n");
            }
        }
        
        if (!extraInJava.isEmpty()) {
            commands.append("\n2. REMOVE from ").append(className).append(".java (or add to XSD if intended):\n");
            for (String attr : extraInJava) {
                commands.append("   - Remove field: ").append(attr).append("\n");
                commands.append("   - Remove getter: get").append(attr.substring(0, 1).toUpperCase()).append(attr.substring(1)).append("()\n");
                commands.append("   - Remove setter: set").append(attr.substring(0, 1).toUpperCase()).append(attr.substring(1)).append("()\n");
            }
        }
        
        if (!missingInJava.isEmpty()) {
            commands.append("\n3. UPDATE generateStatements() method to include new attributes:\n");
            for (String attr : missingInJava) {
                String capitalizedAttr = attr.substring(0, 1).toUpperCase() + attr.substring(1);
                commands.append("   statement.set").append(capitalizedAttr).append("(get").append(capitalizedAttr).append("());\n");
            }
        }
        
        return commands.toString();
    }
}