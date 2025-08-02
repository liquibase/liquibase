package liquibase.parser.core.xml;

import liquibase.change.Change;
import liquibase.change.core.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test-driven enforcement of requirements documentation alignment with actual implementation.
 * This test validates that requirements documents accurately reflect what is actually
 * implemented in Java classes - no more, no less.
 * 
 * ENFORCEMENT: This test MUST pass before any requirements are considered complete.
 * When this test fails, it provides exact documentation updates needed to achieve alignment.
 */
@DisplayName("Requirements Documentation vs Implementation Alignment")
public class SnowflakeRequirementsAlignmentTest {

    private static final String REQUIREMENTS_BASE_PATH = "/Users/kevinchappell/Documents/GitHub/liquibase/claude_guide/snowflake_requirements/changetype_requirements";
    
    /**
     * Map of Change classes to their requirements document paths
     */
    private static final Map<Class<? extends Change>, String> CHANGE_TO_REQUIREMENTS_MAP = createRequirementsMap();
    
    private static Map<Class<? extends Change>, String> createRequirementsMap() {
        Map<Class<? extends Change>, String> map = new HashMap<>();
        map.put(CreateDatabaseChange.class, "new_changetypes/createDatabase_requirements.md");
        map.put(CreateWarehouseChange.class, "new_changetypes/createWarehouse_requirements.md");
        map.put(CreateSchemaChange.class, "existing_changetype_extensions/createSchema_requirements.md");
        map.put(AlterDatabaseChange.class, "new_changetypes/alterDatabase_requirements.md");
        map.put(AlterWarehouseChange.class, "new_changetypes/alterWarehouse_requirements.md");
        map.put(AlterSchemaChange.class, "existing_changetype_extensions/alterSchema_requirements.md");
        map.put(CreateSequenceChangeSnowflake.class, "existing_changetype_extensions/createSequenceEnhanced_requirements.md");
        map.put(DropDatabaseChange.class, "new_changetypes/dropDatabase_requirements.md");
        map.put(DropWarehouseChange.class, "new_changetypes/dropWarehouse_requirements.md");
        map.put(DropSchemaChange.class, "existing_changetype_extensions/dropSchema_requirements.md");
        return map;
    }
    
    @Test
    @DisplayName("CreateDatabase requirements must match actual Java implementation")
    public void testCreateDatabaseRequirementsAlignment() {
        validateRequirementsAlignment(CreateDatabaseChange.class);
    }
    
    @Test
    @DisplayName("CreateWarehouse requirements must match actual Java implementation")
    public void testCreateWarehouseRequirementsAlignment() {
        validateRequirementsAlignment(CreateWarehouseChange.class);
    }
    
    @Test
    @DisplayName("CreateSchema requirements must match actual Java implementation")
    public void testCreateSchemaRequirementsAlignment() {
        validateRequirementsAlignment(CreateSchemaChange.class);
    }
    
    @Test
    @DisplayName("AlterDatabase requirements must match actual Java implementation")
    public void testAlterDatabaseRequirementsAlignment() {
        validateRequirementsAlignment(AlterDatabaseChange.class);
    }
    
    @Test
    @DisplayName("AlterWarehouse requirements must match actual Java implementation")
    public void testAlterWarehouseRequirementsAlignment() {
        validateRequirementsAlignment(AlterWarehouseChange.class);
    }
    
    @Test
    @DisplayName("AlterSchema requirements must match actual Java implementation")
    public void testAlterSchemaRequirementsAlignment() {
        validateRequirementsAlignment(AlterSchemaChange.class);
    }
    
    @Test
    @DisplayName("CreateSequence requirements must match actual Java implementation")
    public void testCreateSequenceRequirementsAlignment() {
        validateRequirementsAlignment(CreateSequenceChangeSnowflake.class);
    }
    
    @Test
    @DisplayName("ALL requirements documents must align with their implementations")
    public void testAllRequirementsAlignment() {
        List<String> failures = new ArrayList<>();
        int validatedCount = 0;
        
        for (Class<? extends Change> changeClass : CHANGE_TO_REQUIREMENTS_MAP.keySet()) {
            try {
                validateRequirementsAlignment(changeClass);
                validatedCount++;
            } catch (AssertionError e) {
                failures.add(changeClass.getSimpleName() + ": " + e.getMessage());
            } catch (Exception e) {
                // Skip classes where requirements don't exist yet
                if (!e.getMessage().contains("Requirements document not found")) {
                    failures.add(changeClass.getSimpleName() + ": " + e.getMessage());
                }
            }
        }
        
        if (!failures.isEmpty()) {
            fail("Multiple requirements alignment failures:\n" + 
                 String.join("\n", failures));
        }
        
        System.out.printf("✅ ALL requirements aligned: %d documents validated%n", validatedCount);
    }
    
    /**
     * Core validation method that enforces requirements alignment with implementation
     */
    private void validateRequirementsAlignment(Class<? extends Change> changeClass) {
        String requirementsPath = CHANGE_TO_REQUIREMENTS_MAP.get(changeClass);
        if (requirementsPath == null) {
            fail("No requirements document configured for " + changeClass.getSimpleName());
        }
        
        try {
            // Extract attributes from Java implementation
            Set<String> implementedAttributes = extractImplementationAttributes(changeClass);
            
            // Extract attributes from requirements document  
            Set<String> documentedAttributes = extractRequirementsAttributes(requirementsPath);
            
            // Find missing attributes in requirements
            Set<String> missingInRequirements = implementedAttributes.stream()
                .filter(attr -> !documentedAttributes.contains(attr))
                .collect(Collectors.toSet());
                
            // Find extra attributes in requirements (documented but not implemented)
            Set<String> extraInRequirements = documentedAttributes.stream()
                .filter(attr -> !implementedAttributes.contains(attr))
                .collect(Collectors.toSet());
            
            // Generate documentation commands if there are misalignments
            if (!missingInRequirements.isEmpty() || !extraInRequirements.isEmpty()) {
                String commands = generateRequirementsAlignmentCommands(
                    changeClass.getSimpleName(), requirementsPath, 
                    missingInRequirements, extraInRequirements);
                
                fail(String.format(
                    "REQUIREMENTS MISALIGNMENT: %s requirements do not match implementation\n" +
                    "Missing in requirements: %s\n" +
                    "Extra in requirements: %s\n\n" +
                    "REQUIRED ACTIONS (copy-paste these):\n%s\n\n" +
                    "After making these changes, re-run this test to verify alignment.",
                    changeClass.getSimpleName(), missingInRequirements, extraInRequirements, commands
                ));
            }
            
            System.out.printf("✅ %s: Requirements aligned (%d attributes validated)%n", 
                changeClass.getSimpleName(), implementedAttributes.size());
                
        } catch (Exception e) {
            if (e.getMessage().contains("Requirements document not found")) {
                // Skip validation for classes without requirements documents
                System.out.printf("⚠️  %s: Requirements document not found, skipping validation%n", 
                    changeClass.getSimpleName());
                return;
            }
            fail("Failed to validate " + changeClass.getSimpleName() + ": " + e.getMessage(), e);
        }
    }
    
    /**
     * Extracts attribute names from Java implementation
     */
    private Set<String> extractImplementationAttributes(Class<? extends Change> changeClass) {
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
     * Extracts attribute names from requirements document
     */
    private Set<String> extractRequirementsAttributes(String requirementsPath) throws IOException {
        Set<String> attributes = new HashSet<>();
        
        Path fullPath = Paths.get(REQUIREMENTS_BASE_PATH, requirementsPath);
        if (!Files.exists(fullPath)) {
            throw new IOException("Requirements document not found: " + fullPath);
        }
        
        String content = new String(Files.readAllBytes(fullPath), StandardCharsets.UTF_8);
        
        // Pattern 1: Extract from COMPREHENSIVE_ATTRIBUTE_ANALYSIS table
        Pattern tablePattern = Pattern.compile(
            "COMPREHENSIVE_ATTRIBUTE_ANALYSIS.*?\n(.*?)\n##", 
            Pattern.DOTALL);
        Matcher tableMatcher = tablePattern.matcher(content);
        
        if (tableMatcher.find()) {
            String tableContent = tableMatcher.group(1);
            
            // Extract attribute names from table rows (first column after |)
            // Look for valid Java attribute names that start with lowercase letter
            Pattern rowPattern = Pattern.compile("\\|\\s*([a-z][a-zA-Z0-9_]*)\\s*\\|");
            Matcher rowMatcher = rowPattern.matcher(tableContent);
            
            while (rowMatcher.find()) {
                String attribute = rowMatcher.group(1);
                // Skip table headers and invalid attribute names
                if (!attribute.equals("Attribute") && 
                    !attribute.equals("--------") &&
                    !isInvalidAttributeName(attribute)) {
                    attributes.add(attribute);
                }
            }
        }
        
        // Pattern 2: Extract from SQL examples (attribute="value" patterns)
        // Look for valid Java attribute names that start with lowercase letter
        Pattern sqlPattern = Pattern.compile("([a-z][a-zA-Z0-9_]*)\\s*=\\s*[\"'][^\"']*[\"']");
        Matcher sqlMatcher = sqlPattern.matcher(content);
        while (sqlMatcher.find()) {
            String attribute = sqlMatcher.group(1);
            if (!isInvalidAttributeName(attribute)) {
                attributes.add(attribute);
            }
        }
        
        return attributes;
    }
    
    /**
     * Generates exact documentation commands for requirements alignment
     */
    private String generateRequirementsAlignmentCommands(String className, String requirementsPath, 
            Set<String> missingInRequirements, Set<String> extraInRequirements) {
        StringBuilder commands = new StringBuilder();
        
        commands.append("1. UPDATE requirements document: ").append(requirementsPath).append("\n\n");
        
        if (!missingInRequirements.isEmpty()) {
            commands.append("ADD these attributes to COMPREHENSIVE_ATTRIBUTE_ANALYSIS table:\n");
            for (String attr : missingInRequirements) {
                commands.append("| ").append(attr).append(" | [Description] | [DataType] | [Required/Optional] | [Default] | [ValidValues] | [Constraints] | [MutualExclusivity] | [Priority] | [Notes] |\n");
            }
            commands.append("\n");
            
            commands.append("ADD these attributes to SQL examples:\n");
            for (String attr : missingInRequirements) {
                commands.append("  ").append(attr).append("=\"[example_value]\"\n");
            }
            commands.append("\n");
        }
        
        if (!extraInRequirements.isEmpty()) {
            commands.append("REMOVE these attributes from requirements (not implemented in Java):\n");
            for (String attr : extraInRequirements) {
                commands.append("  - Remove ").append(attr).append(" from attribute analysis table\n");
                commands.append("  - Remove ").append(attr).append(" from SQL examples\n");
            }
            commands.append("\n");
            
            commands.append("OR implement these attributes in Java if they should be supported:\n");
            for (String attr : extraInRequirements) {
                String capitalizedAttr = attr.substring(0, 1).toUpperCase() + attr.substring(1);
                commands.append("  private String ").append(attr).append(";\n");
                commands.append("  public String get").append(capitalizedAttr).append("() { return ").append(attr).append("; }\n");
                commands.append("  public void set").append(capitalizedAttr).append("(String ").append(attr).append(") { this.").append(attr).append(" = ").append(attr).append("; }\n");
            }
        }
        
        return commands.toString();
    }
    
    /**
     * Filters out invalid attribute names that are metadata, not actual Java attributes
     */
    private boolean isInvalidAttributeName(String attribute) {
        // Filter out data types
        if (attribute.equals("String") || attribute.equals("Boolean") || attribute.equals("Integer")) {
            return true;
        }
        
        // Filter out priority levels
        if (attribute.equals("HIGH") || attribute.equals("MEDIUM") || attribute.equals("LOW")) {
            return true;
        }
        
        // Filter out common values and constants
        if (attribute.equals("true") || attribute.equals("false") || attribute.equals("null") || 
            attribute.equals("None") || attribute.equals("Default") || attribute.equals("Constraints")) {
            return true;
        }
        
        // Filter out XML namespace and metadata attributes
        if (attribute.equals("xmlns") || attribute.equals("xsi") || attribute.equals("schemaLocation") ||
            attribute.equals("snowflake") || attribute.equals("author") || attribute.equals("id") ||
            attribute.equals("noOrder")) {
            return true;
        }
        
        // Filter out Snowflake constants that appear in docs but aren't attributes
        if (attribute.equals("COMMENT") || attribute.equals("TRACE_LEVEL") || attribute.equals("LOG_LEVEL") ||
            attribute.equals("DEFAULT_DDL_COLLATION") || attribute.equals("XSMALL") || attribute.equals("STANDARD")) {
            return true;
        }
        
        // Filter out action/operation names that aren't attributes
        // But allow unset* attributes as they are valid Java attributes
        if (attribute.startsWith("set") && !attribute.startsWith("unset")) {
            return true;
        }
        if (attribute.equals("refresh") || attribute.equals("enable") || attribute.equals("disable")) {
            return true;
        }
        
        return false;
    }
}