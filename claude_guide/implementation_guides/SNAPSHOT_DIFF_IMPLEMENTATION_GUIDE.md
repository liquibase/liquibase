# Complete Snapshot/Diff Implementation Guide
## AI-Optimized Single-Source Workflow

## 🎯 START HERE

**Complete snapshot/diff implementation workflow** with extension object patterns:

```
PHASE 0: Quick Assessment + Cost Analysis → PHASE 1: Requirements Research → PHASE 2: Object Model → PHASE 3: Snapshot Implementation → PHASE 4: Diff Implementation → PHASE 5: Strategic Testing
```

**Core focus**: Database introspection, schema comparison, and extension object discovery
**Prerequisites**: Basic understanding of Liquibase object model + **Cost consciousness for Snowflake testing**
**Critical decision**: Schema-level vs Account-level objects require different implementation patterns
**Outcome**: Working snapshot generators and diff comparators with strategic test coverage

## 💰 **COST-CONSCIOUS DEVELOPMENT (Added from Session)**

### **Snowflake Integration Testing Costs Real Money**
**LEARNED**: Every integration test execution costs Snowflake compute credits. Be strategic.

### **Cost-Effective Testing Strategy**
```yaml
HIGH_VALUE_TESTS:
  - "Core snapshot functionality": Essential for business logic
  - "End-to-end diff workflows": Real user scenarios
  - "Data type precision": Prevents data corruption
  
LOW_VALUE_TESTS:
  - "Console warning capture": Unit tests sufficient
  - "Error message formatting": Mock tests sufficient  
  - "Edge case scenarios": Can be unit tested

COST_OPTIMIZATION:
  - "Unit test coverage > 85%": Reduce integration test dependency
  - "Mock heavy scenarios": Avoid live database when possible
  - "Parallel test execution": Reduce total runtime costs
```

## ⚡ CRITICAL: EXTENSION OBJECT PATTERNS

### 🔑 ARCHITECTURAL DISCOVERY (Session 2025-08-07)
**Extension objects require different patterns than core Liquibase objects.**

#### Extension Object Categories
```yaml
SCHEMA_LEVEL_OBJECTS:
  EXAMPLES: ["FileFormat", "Stage", "Pipe"]
  PARENT_RELATIONSHIP: "Schema.class"
  DISCOVERY_PATTERN: "INFORMATION_SCHEMA queries with schema parameter"
  IMPLEMENTATION: "Standard SnapshotGenerator patterns work"
  
ACCOUNT_LEVEL_OBJECTS:
  EXAMPLES: ["Warehouse", "User", "Role", "ResourceMonitor"]  
  PARENT_RELATIONSHIP: "Account.class"
  DISCOVERY_PATTERN: "SHOW commands (no schema parameter)"
  IMPLEMENTATION: "Requires unified extensibility framework"
  
CRITICAL_LIMITATION:
  ISSUE: "Extension → extension addsTo() relationships don't work in Liquibase core"
  ROOT_CAUSE: "Core assumes schema-based discovery patterns"
  SOLUTION: "SnowflakeExtensionDiffGeneratorSimple (validated working)"
```

#### 🚨 When Standard Patterns Fail
```yaml
SYMPTOMS:
  - "SnapshotGenerator addTo() method never called"
  - "Objects not discovered in snapshots despite correct priority"
  - "snapshot.get(ObjectType.class) returns empty/null"
  
IMPLEMENTATION_DECISION:
  SCHEMA_LEVEL: "Use standard SnapshotGenerator patterns"
  ACCOUNT_LEVEL: "Use unified extensibility framework"
  MIXED: "Combine both approaches based on object type"
```

## IMPLEMENTATION DECISION TREE

**Critical first decision - determines entire implementation approach:**

```
What type of database object are you implementing?

SCHEMA-LEVEL OBJECTS (8-12 hours)
├─ Examples: FileFormat, Stage, Pipe, View, Table
├─ Pattern: Standard SnapshotGenerator approach works
├─ Discovery: INFORMATION_SCHEMA queries with schema parameter
└─ Phases: 0 → 1 → 2 → 3 → 4

ACCOUNT-LEVEL OBJECTS (12-16 hours)  
├─ Examples: Warehouse, User, Role, ResourceMonitor
├─ Pattern: Requires unified extensibility framework
├─ Discovery: SHOW commands (no schema parameter)
└─ Phases: 0 → Extensibility Framework → 1 → 2 → 3 → 4

Already have partial implementation?
├─ Missing snapshot only → Skip to Phase 3 (4-6 hours)
├─ Missing diff only → Skip to Phase 4 (2-4 hours)  
├─ Bug fixes → Skip to Phase 4 debugging (2-4 hours)
└─ Performance issues → Phase 3 + 4 optimization (3-5 hours)
```

**Most critical**: Identifying schema-level vs account-level early prevents major rework

## PHASE 0: QUICK VALIDATION AND ASSESSMENT (OPTIONAL)

### When to Use Quick Validation
- Checking if snapshot/diff already exists for object type
- Quick database connectivity and scope validation
- 15-minute assessment before full implementation
- Validating realistic success criteria for test harness

### STEP 0.1: Quick Database Object Validation
```bash
# Test database connectivity and object queryability
echo "SELECT * FROM INFORMATION_SCHEMA.${OBJECT_TYPE}S LIMIT 1" | sqlcmd

# Check if snapshot/diff already implemented
find . -name '*${ObjectType}*' -path '*/snapshot/*' -o -path '*/diff/*'

# Verify object creation permissions
echo "CREATE ${OBJECT_TYPE} test_validation_temp" | sqlcmd
echo "DROP ${OBJECT_TYPE} test_validation_temp" | sqlcmd
```

```yaml
SCOPE_ASSESSMENT_CHECKLIST:
  EXISTING_IMPLEMENTATION:
    - "Check if snapshot generator already exists"
    - "Check if diff comparator already exists" 
    - "Determine enhancement vs new implementation"
    
  CORE_OBJECT_MODEL:
    - "Check if Liquibase core has object model"
    - "Assess if extending existing vs creating new"
    
  DATABASE_COMPATIBILITY:
    - "Verify object appears in INFORMATION_SCHEMA"
    - "Test object creation and deletion permissions"
    - "Confirm all properties are queryable"
    
VALIDATION_COMMANDS:
  CHECK_CORE_OBJECT: "find . -name '*${ObjectType}*.java' -path '*/liquibase-core/*'"
  CHECK_SNAPSHOT: "find . -name '*${ObjectType}*SnapshotGenerator*'"
  CHECK_COMPARATOR: "find . -name '*${ObjectType}*Comparator*'"
```

### STEP 0.3: Implementation Scope Decision
```yaml
SCOPE_DECISION_MATRIX:
  IF_CORE_OBJECT_EXISTS:
    PATTERN: "EXTEND_EXISTING_OBJECT"
    APPROACH: "Enhance existing object model with database-specific properties"
    COMPLEXITY: "Medium"
    
  IF_NO_CORE_OBJECT:
    PATTERN: "NEW_OBJECT_IMPLEMENTATION" 
    APPROACH: "Create complete object model, snapshot, and diff"
    COMPLEXITY: "High"
    
  IF_SNAPSHOT_EXISTS_DIFF_MISSING:
    PATTERN: "COMPLETE_DIFF_ONLY"
    APPROACH: "Implement comparator for existing snapshot"
    COMPLEXITY: "Low"
    
  IF_DIFF_EXISTS_SNAPSHOT_MISSING:
    PATTERN: "COMPLETE_SNAPSHOT_ONLY"
    APPROACH: "Implement snapshot generator for existing diff"
    COMPLEXITY: "Medium"
```

## PHASE 1: REQUIREMENTS RESEARCH AND DOCUMENTATION

### APPROACH SELECTION: MANUAL vs TASK-DELEGATED RESEARCH

**Choose your approach based on object complexity and research depth needed:**

#### OPTION A: MANUAL RESEARCH (2-4 hours)
Best for: Extending existing objects, simple property additions, direct control needed

#### OPTION B: TASK-DELEGATED RESEARCH (30 min setup + autonomous execution) 
Best for: New object types, comprehensive property analysis, parallel work desired

---

## OPTION A: MANUAL RESEARCH WORKFLOW

### STEP 1.1: Official Documentation Research
```yaml
RESEARCH_PROCESS:
  OFFICIAL_DOCUMENTATION:
    OBJECTIVE: "Document complete syntax and behavior from vendor documentation"
    SOURCES:
      - "Database vendor official SQL reference"
      - "CREATE/ALTER/DROP/SHOW command documentation"
      - "INFORMATION_SCHEMA documentation"
      - "System catalog documentation"
      
  RESEARCH_COMMANDS:
    FIND_CREATE_SYNTAX: "Search '[database] CREATE [object] syntax'"
    FIND_ALTER_SYNTAX: "Search '[database] ALTER [object] syntax'"
    FIND_SYSTEM_VIEWS: "Search '[database] [object] system views'"
    FIND_INFORMATION_SCHEMA: "Search '[database] INFORMATION_SCHEMA [object]'"
```

### STEP 1.2: Property Analysis and Categorization
```yaml
PROPERTY_CATEGORIZATION:
  REQUIRED_PROPERTIES:
    DEFINITION: "Must be specified at object creation"
    SNAPSHOT_REQUIREMENT: "Must be captured in snapshot"
    DIFF_REQUIREMENT: "Must be compared for differences"
    
  OPTIONAL_CONFIGURATION:
    DEFINITION: "Can be set/changed by user"
    SNAPSHOT_REQUIREMENT: "Capture when non-default"
    DIFF_REQUIREMENT: "Compare when present"
    
  STATE_PROPERTIES:
    DEFINITION: "Runtime information, read-only"
    SNAPSHOT_REQUIREMENT: "Capture for reference only"
    DIFF_REQUIREMENT: "EXCLUDE from comparison"
    
  SYSTEM_METADATA:
    DEFINITION: "Database-managed timestamps, IDs"
    SNAPSHOT_REQUIREMENT: "Usually exclude"
    DIFF_REQUIREMENT: "EXCLUDE from comparison"
```

### STEP 1.3: SQL Query Development
```yaml
SNAPSHOT_SQL_DEVELOPMENT:
  BASE_QUERY_PATTERN: |
    SELECT 
        object_name,
        property1,
        property2,
        CASE WHEN property3 = 'DEFAULT' THEN NULL ELSE property3 END as property3
    FROM INFORMATION_SCHEMA.${OBJECT_TYPE}S
    WHERE object_catalog = ? 
    AND object_schema = ?
    [AND object_name = ?]
    
  OPTIMIZATION_PATTERNS:
    NULL_HANDLING: "Use CASE statements for default value normalization"
    TYPE_CONVERSION: "Convert database types to Java types"
    FILTERING: "Include WHERE clauses for scope limitation"
    ORDERING: "Use ORDER BY for consistent results"
```

### STEP 1.4: Requirements Document Template
```yaml
REQUIREMENTS_DOCUMENT_STRUCTURE:
  SECTIONS:
    - "Object Overview and Purpose"
    - "Complete Property Analysis Table"
    - "SQL Query Specifications"
    - "Comparison Logic Requirements"
    - "Test Scenarios and Edge Cases"
    - "Framework Integration Requirements"
    
PROPERTY_ANALYSIS_TABLE:
  COLUMNS: ["Property", "Type", "Required", "Default", "Comparison", "Notes"]
  EXAMPLE: |
    | Property | Type | Required | Default | Comparison | Notes |
    |----------|------|----------|---------|------------|-------|
    | name | String | Yes | - | Full | Object identifier |
    | comment | String | No | NULL | Exclude if both null | User description |
    | created_time | Timestamp | No | CURRENT_TIME | Exclude | System metadata |
```

---

## OPTION B: TASK-DELEGATED RESEARCH WORKFLOW

### WHEN TO USE TASK DELEGATION
- **New database object types** with unknown properties and behavior
- **Complex objects** with many optional properties and constraints
- **Comprehensive analysis needed** across multiple information sources
- **Want to work on implementation** while research runs autonomously

### TASK DELEGATION SETUP

#### STEP 1B.1: LAUNCH SNAPSHOT/DIFF REQUIREMENTS RESEARCH TASK
```markdown
Task(
  subagent_type: "general-purpose", 
  description: "Comprehensive snapshot/diff requirements research for [OBJECT_TYPE]",
  prompt: "Research and create complete snapshot/diff requirements for Snowflake [OBJECT_TYPE]:

RESEARCH_OBJECTIVES:
1. OFFICIAL_DOCUMENTATION_ANALYSIS:
   - Find Snowflake official documentation for [OBJECT_TYPE]
   - Document CREATE/ALTER/DROP/SHOW syntax and all parameters
   - Extract complete property list with descriptions and constraints
   - Document relationships to other objects (dependencies, references)

2. INFORMATION_SCHEMA_INVESTIGATION:
   - Identify INFORMATION_SCHEMA views that contain [OBJECT_TYPE] metadata
   - Document available columns and their meanings
   - Test queryability of all properties
   - Cross-reference with official documentation to identify gaps

3. EXISTING_IMPLEMENTATION_ANALYSIS:
   - Search liquibase-snowflake codebase for similar object patterns
   - Analyze existing snapshot generators (Table, Sequence, Warehouse, etc.)
   - Document reusable patterns and architectural approaches
   - Identify object model structure requirements

4. PROPERTY_CATEGORIZATION_AND_COMPARISON_ANALYSIS:
   For each identified property, determine:
   - **Snapshot Inclusion**: Should this property be captured in snapshots?
   - **Comparison Strategy**: How should differences be detected? (Full comparison, exclude if both null, ignore completely)
   - **Required vs Optional**: Is this property required for object creation?
   - **State vs Configuration**: Is this a user-settable property or system state?

5. COMPREHENSIVE_REQUIREMENTS_DOCUMENT:
   Create structured document with:
   - Complete property table with snapshot/comparison strategy
   - Object model class structure recommendations
   - INFORMATION_SCHEMA query patterns for snapshot generation
   - Diff comparison logic recommendations
   - Edge cases and special handling requirements
   - Test scenario recommendations

DELIVERABLE: Complete snapshot/diff requirements document ready for Phase 2 (Object Model Implementation)"
)
```

#### STEP 1B.2: TASK OUTPUT VALIDATION
While Task runs autonomously, prepare for validation:

**Task Completion Checklist:**
- [ ] Official Snowflake documentation analyzed and referenced  
- [ ] Complete property list with types and constraints documented
- [ ] INFORMATION_SCHEMA views identified and column mappings created
- [ ] Property categorization completed (snapshot inclusion, comparison strategy)
- [ ] Object model structure recommendations provided
- [ ] Existing implementation patterns analyzed and documented
- [ ] Edge cases and special handling requirements identified
- [ ] Test scenarios recommended based on property combinations

**Quality Gates:**
- All properties have clear snapshot inclusion decisions
- Comparison strategies defined for each property type
- No "TODO" or "Unknown" entries in property analysis
- Object model recommendations are architecturally sound
- INFORMATION_SCHEMA queries are testable and complete

#### STEP 1B.3: REQUIREMENTS INTEGRATION
Once Task completes:

1. **Review Task Output** against validation checklist
2. **Validate INFORMATION_SCHEMA queries** with database if available
3. **Cross-check object model recommendations** against existing patterns  
4. **Proceed to Phase 2** with comprehensive requirements

### TASK OUTPUT TEMPLATE
The Task should produce a requirements document with this structure:

```markdown
# [OBJECT_TYPE] Snapshot/Diff Requirements Documentation

## Official Documentation Reference
- **URL**: [Snowflake official docs URL]
- **Key Properties**: [Summary of main configurable properties] 
- **Dependencies**: [Related objects and constraints]

## Property Analysis Table
| Property | Type | Snapshot Include | Comparison Strategy | Required | Default | Notes |
|----------|------|------------------|-------------------|----------|---------|-------|
| [prop1] | [type] | [Yes/No] | [Full/Exclude if both null/Ignore] | [Y/N] | [default] | [notes] |

## INFORMATION_SCHEMA Analysis
### Primary View: [VIEW_NAME]
- **Location**: `INFORMATION_SCHEMA.[VIEW_NAME]`
- **Key Columns**: [List of relevant columns]
- **Query Pattern**: [Sample query for snapshot generation]

### Property Mappings
| Property | INFORMATION_SCHEMA Column | Data Type | Special Handling |
|----------|--------------------------|-----------|------------------|
| [prop] | [column] | [type] | [any special processing needed] |

## Object Model Recommendations
### Class Structure
[Recommended Java class structure with key properties]

### Architectural Pattern
[Extend existing vs create new, integration points]

## Comparison Logic Recommendations
### Full Comparison Properties
[Properties that should always be compared for differences]

### Conditional Comparison Properties  
[Properties with special comparison rules]

### Excluded Properties
[Properties to ignore in diff comparison]

## Implementation Patterns from Similar Objects
[Reusable patterns from existing implementations]

## Test Scenarios
[Recommended test cases covering property combinations and edge cases]
```

---

## PHASE 2: OBJECT MODEL IMPLEMENTATION

### STEP 2.1: Database Object Model Creation
```yaml
OBJECT_MODEL_PATTERN:
  CLASS_STRUCTURE: |
    public class ${ObjectType} extends AbstractDatabaseObject {
        private String name;
        private String catalogName;
        private String schemaName;
        
        // Required properties
        private String requiredProperty1;
        private Boolean requiredProperty2;
        
        // Optional configuration properties
        private String optionalProperty1;
        private Integer optionalProperty2;
        
        // State properties (read-only)
        private Date createdTime;
        private String systemId;
        
        // Constructors, getters, setters
        // equals(), hashCode(), toString()
    }
    
  CRITICAL_METHODS:
    EQUALS_HASHCODE: "Based on name, catalogName, schemaName only"
    TO_STRING: "Include all properties for debugging"
    GET_OBJECT_TYPE_NAME: "Return object type for framework"
```

### STEP 2.2: Property Normalization and Validation
```yaml
NORMALIZATION_PATTERNS:
  STRING_NORMALIZATION: "null/empty/\"NULL\" → null, trim whitespace"
  BOOLEAN_NORMALIZATION: "null/\"NULL\" → null, parse boolean values"
  VALIDATION_REQUIRED: "Check required fields, return ValidationErrors"
```

## PHASE 3: SNAPSHOT IMPLEMENTATION

### STEP 3.1: SnapshotGenerator Implementation
```yaml
SNAPSHOT_GENERATOR_PATTERN:
  CLASS_STRUCTURE: |
    public class ${ObjectType}SnapshotGeneratorSnowflake extends SnapshotGenerator {
        @Override
        protected Class<? extends DatabaseObject>[] getOtherObjectTypes() {
            return new Class[]{${ObjectType}.class};
        }
        
        @Override
        protected DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) 
                throws DatabaseException, InvalidExampleException {
            return snapshot${ObjectType}((${ObjectType}) example, snapshot);
        }
        
        protected ${ObjectType} snapshot${ObjectType}(${ObjectType} example, DatabaseSnapshot snapshot) 
                throws DatabaseException {
            // Implementation details
        }
    }
    
SQL_QUERY_IMPLEMENTATION:
  BASE_PATTERN: |
    protected ${ObjectType} snapshot${ObjectType}(${ObjectType} example, DatabaseSnapshot snapshot) 
            throws DatabaseException {
        Database database = snapshot.getDatabase();
        
        String sql = "SELECT name, property1, property2 FROM INFORMATION_SCHEMA.${OBJECT_TYPE}S " +
                    "WHERE catalog_name = ? AND schema_name = ?";
        List<Object> params = Arrays.asList(
            database.getDefaultCatalogName(), 
            database.getDefaultSchemaName()
        );
        
        if (example.getName() != null) {
            sql += " AND name = ?";
            params.add(example.getName());
        }
        
        List<Map<String, ?>> results = ExecutorService.getInstance()
            .getExecutor(database).queryForList(new RawParameterizedSql(sql, params.toArray()));
            
        Set<${ObjectType}> objects = new HashSet<>();
        for (Map<String, ?> row : results) {
            ${ObjectType} obj = new ${ObjectType}();
            obj.setName((String) row.get("NAME"));
            obj.setProperty1((String) row.get("PROPERTY1"));
            obj.setProperty2(normalizeBoolean((String) row.get("PROPERTY2")));
            objects.add(obj);
        }
        
        return example.getName() != null ? 
            objects.stream().findFirst().orElse(null) : null;
    }
```

### STEP 3.2: Service Registration and Testing
```yaml
SERVICE_REGISTRATION:
  FILE: "src/main/resources/META-INF/services/liquibase.snapshot.SnapshotGenerator"
  ENTRY: "${PACKAGE}.${ObjectType}SnapshotGeneratorSnowflake"
  
UNIT_TEST_PATTERN:
  CRITICAL_APPROACH: "Complete SQL String Assertions (Primary Pattern)"
  SETUP: "Mock database and snapshot, configure mock results"
  EXECUTE: "Call snapshotObject with example"
  VERIFY: "Assert complete SQL strings, not components"
  
COMPLETE_SQL_ASSERTION_PATTERN:
  PRIMARY_APPROACH: |
    @Test
    void testMethod_CompleteSQL_Scenario() {
        // Execute method that generates SQL
        String actualSQL = generator.generateSQL(parameters);
        
        // Assert complete SQL string (NOT components)
        String expectedSQL = "SELECT COLUMN1, COLUMN2 FROM INFORMATION_SCHEMA.TABLES WHERE CATALOG=? AND SCHEMA=?";
        assertEquals(expectedSQL, actualSQL, "Should generate correct complete SQL");
    }
  
  WHY_SUPERIOR:
    - "More reliable: catches formatting and ordering issues"
    - "Maintainable: single assertion point"
    - "Real-world: tests actual database execution"
    - "User feedback: 'testing the completed SQL string is a better test'"
  
  GOOD_EXAMPLE: |
    String expectedSQL = "SHOW WAREHOUSES";
    assertEquals(expectedSQL, actualSQL, "Should generate correct complete SQL");
  
  AVOID_PATTERN: |
    // FRAGILE: Component-based assertions
    assertTrue(actualSQL.contains("SHOW"));
    assertTrue(actualSQL.contains("WAREHOUSES"));
    // Multiple assertions miss integration issues
```

## PHASE 4: DIFF IMPLEMENTATION

### STEP 4.1: DatabaseObjectComparator Implementation
```yaml
COMPARATOR_PATTERN:
  CLASS_STRUCTURE: |
    public class ${ObjectType}ComparatorSnowflake extends DatabaseObjectComparator {
        @Override
        public boolean supports(Class<? extends DatabaseObject> type, Database database) {
            return ${ObjectType}.class.isAssignableFrom(type) && 
                   database instanceof SnowflakeDatabase;
        }
        
        @Override
        public ObjectDifferences compare(DatabaseObject referenceObject, 
                                       DatabaseObject comparisonObject, 
                                       DatabaseSnapshot referenceSnapshot,
                                       DatabaseSnapshot comparisonSnapshot, 
                                       CompareControl compareControl) {
            return compare${ObjectType}((${ObjectType}) referenceObject, 
                                      (${ObjectType}) comparisonObject, 
                                      compareControl);
        }
        
        protected ObjectDifferences compare${ObjectType}(${ObjectType} reference, 
                                                       ${ObjectType} comparison, 
                                                       CompareControl compareControl) {
            ObjectDifferences differences = new ObjectDifferences(compareControl);
            
            // Compare each property with appropriate logic
            compareProperty(differences, "property1", 
                          reference.getProperty1(), comparison.getProperty1());
            compareProperty(differences, "property2", 
                          reference.getProperty2(), comparison.getProperty2());
            
            return differences;
        }
    }
    
PROPERTY_COMPARISON_PATTERNS:
  STRING_COMPARISON: |
    private void compareProperty(ObjectDifferences differences, String propertyName, 
                               String reference, String comparison) {
        reference = normalizeString(reference);
        comparison = normalizeString(comparison);
        
        if (!Objects.equals(reference, comparison)) {
            differences.addDifference(propertyName, reference, comparison);
        }
    }
    
  BOOLEAN_COMPARISON: |
    private void compareProperty(ObjectDifferences differences, String propertyName, 
                               Boolean reference, Boolean comparison) {
        if (!Objects.equals(reference, comparison)) {
            differences.addDifference(propertyName, reference, comparison);
        }
    }
    
  EXCLUDE_STATE_PROPERTIES: |
    // DO NOT compare state properties like createdTime, systemId
    // These are read-only and should not affect diff results
```

### STEP 4.2: Service Registration and Testing
```yaml
SERVICE_REGISTRATION:
  FILE: "src/main/resources/META-INF/services/liquibase.diff.output.DatabaseObjectComparator"
  ENTRY: "${PACKAGE}.${ObjectType}ComparatorSnowflake"
  
UNIT_TEST_SCENARIOS:
  NO_DIFFERENCES: "Identical objects should have no differences"
  PROPERTY_DIFFERENCES: "Changed properties should create differences"
  STATE_EXCLUSION: "State properties should not create differences"
```

## PHASE 5: COMPREHENSIVE TESTING AND VALIDATION

### STEP 5.1: Integration Test Implementation
```yaml
INTEGRATION_TEST_PATTERN:
  WORKFLOW: "Create object → Snapshot → Verify → Diff → Cleanup"
  VALIDATION: "Object found in snapshot, properties correct, diff detects changes"
  
TEST_HARNESS_LIMITATIONS:
  - "May not support all object types"
  - "Some properties not testable in isolation"
  - "Cross-database compatibility limited"
  
WORKAROUND_STRATEGIES:
  - "Manual integration tests for unsupported objects"
  - "Focus on realistic success criteria"
```

### STEP 5.2: Error Patterns and Debugging
```yaml
COMMON_ERROR_PATTERNS:
  SERVICE_REGISTRATION_MISSING:
    SYMPTOM: "Objects not found in snapshot/diff"
    CAUSE: "Missing or incorrect META-INF/services registration"
    SOLUTION: "Verify service files contain correct class names"
    COMMAND: "grep -r '${ObjectType}' src/main/resources/META-INF/services/"
    
  SQL_QUERY_ERRORS:
    SYMPTOM: "DatabaseException during snapshot"
    CAUSE: "Invalid SQL syntax or missing columns"
    SOLUTION: "Test SQL queries directly in database console"
    COMMAND: "Execute snapshot SQL manually to verify syntax"
    
  CASE_SENSITIVITY_ISSUES:
    SYMPTOM: "Objects found manually but not in snapshot"
    CAUSE: "Database case sensitivity differs from expectations"
    SOLUTION: "Use UPPER() or database-specific case handling"
    EXAMPLE: "WHERE UPPER(object_name) = UPPER(?)"
    
  PROPERTY_NORMALIZATION_ERRORS:
    SYMPTOM: "Unexpected differences in diff results"
    CAUSE: "Inconsistent null/default value handling"
    SOLUTION: "Implement consistent normalization methods"
    PATTERN: |
      private String normalizeString(String value) {
          if (value == null || "NULL".equalsIgnoreCase(value) || value.trim().isEmpty()) {
              return null;
          }
          return value.trim();
      }
      
  STATE_PROPERTY_COMPARISON:
    SYMPTOM: "Diff shows differences for identical objects"
    CAUSE: "Comparing read-only/system properties"
    SOLUTION: "Exclude state properties from comparison"
    CRITICAL: "Never compare timestamps, system IDs, or auto-generated values"
```

### STEP 5.3: Performance Optimization
```yaml
OPTIMIZATION_PATTERNS:
  SQL_QUERY_OPTIMIZATION:
    - "Use specific WHERE clauses to limit result sets"
    - "Add ORDER BY for consistent results"
    - "Consider database-specific query hints"
    - "Batch queries when possible"
    
  OBJECT_CREATION_OPTIMIZATION:
    - "Reuse object instances when appropriate"
    - "Minimize database round trips"
    - "Cache frequently accessed metadata"
    - "Use connection pooling effectively"
    
  MEMORY_OPTIMIZATION:
    - "Stream large result sets when possible"
    - "Avoid holding entire snapshots in memory"
    - "Clean up resources promptly"
    - "Use appropriate collection types"
```

## OBJECT-SPECIFIC IMPLEMENTATION TEMPLATES

### Database Object Template
```yaml
DATABASE_OBJECT_PATTERN:
  TYPICAL_PROPERTIES:
    - "name (required)"
    - "comment (optional)"
    - "owner (state property - exclude from diff)"
    - "created_time (state property - exclude from diff)"
    
  SNAPSHOT_SQL: |
    SELECT 
        database_name as name,
        comment,
        database_owner as owner,
        created as created_time
    FROM INFORMATION_SCHEMA.DATABASES
    WHERE database_name = ?
    
  COMPARISON_FOCUS:
    - "Compare name and comment only"
    - "Exclude owner and created_time"
    - "Handle null comments consistently"
```

### Schema Object Template
```yaml
SCHEMA_OBJECT_PATTERN:
  TYPICAL_PROPERTIES:
    - "catalogName, schemaName (required)"
    - "comment (optional)"
    - "managedAccess (optional boolean)"
    - "owner (state property - exclude)"
    
  SNAPSHOT_SQL: |
    SELECT 
        catalog_name,
        schema_name,
        comment,
        is_managed_access = 'YES' as managed_access,
        schema_owner as owner
    FROM INFORMATION_SCHEMA.SCHEMATA
    WHERE catalog_name = ? AND schema_name = ?
    
  COMPARISON_FOCUS:
    - "Compare catalogName, schemaName, comment, managedAccess"
    - "Exclude owner"
    - "Handle boolean conversion properly"
```

### Table Object Template
```yaml
TABLE_OBJECT_PATTERN:
  TYPICAL_PROPERTIES:
    - "catalogName, schemaName, tableName (required)"
    - "tableType (required)"
    - "comment (optional)"
    - "clustering, partitioning options (optional)"
    - "rowCount, bytes (state properties - exclude)"
    
  SNAPSHOT_SQL: |
    SELECT 
        table_catalog,
        table_schema,
        table_name,
        table_type,
        comment,
        clustering_key,
        row_count,
        bytes
    FROM INFORMATION_SCHEMA.TABLES
    WHERE table_catalog = ? AND table_schema = ? 
    [AND table_name = ?]
    
  COMPARISON_FOCUS:
    - "Compare name, type, comment, clustering"
    - "Exclude rowCount, bytes"
    - "Handle different table types appropriately"
```

### Sequence Object Template
```yaml
SEQUENCE_OBJECT_PATTERN:
  TYPICAL_PROPERTIES:
    - "catalogName, schemaName, sequenceName (required)"
    - "startValue, incrementBy (optional with defaults)"
    - "minValue, maxValue (optional)"
    - "comment, ordered (optional)"
    - "nextValue (state property - exclude)"
    
  SNAPSHOT_SQL: |
    SELECT 
        sequence_catalog,
        sequence_schema,
        sequence_name,
        start_value,
        increment,
        minimum_value,
        maximum_value,
        cycle_option,
        ordered,
        comment,
        next_value
    FROM INFORMATION_SCHEMA.SEQUENCES
    WHERE sequence_catalog = ? AND sequence_schema = ?
    [AND sequence_name = ?]
    
  COMPARISON_FOCUS:
    - "Compare all configuration properties"
    - "Exclude nextValue (state property)"
    - "Handle default values consistently"
```

## 🔧 UNIFIED EXTENSIBILITY FRAMEWORK

### When to Use Unified Framework
```yaml
USE_WHEN:
  - "Account-level objects (Warehouse, User, Role)"
  - "Extension → extension addsTo() relationships fail"
  - "Standard snapshot patterns don't work"
  - "Objects not discovered despite correct priorities"
  
INDICATORS_OF_NEED:
  - "addTo() method never called (no debug output)"
  - "snapshot.get(ObjectType.class) returns empty"
  - "Objects exist in database but not in snapshots"
```

### Framework Components
```yaml
SNAPSHOT_GENERATOR_PATTERN:
  BASE_CLASS: "ExtensionObjectSnapshotGenerator<T>"
  LOCATION: "src/main/java/liquibase/snapshot/jvm/"
  EXTENDS: "JdbcSnapshotGenerator"
  
DIFF_GENERATOR_PATTERN:
  CLASS: "SnowflakeExtensionDiffGenerator"
  LOCATION: "src/main/java/liquibase/diff/"
  IMPLEMENTS: "DiffGenerator"
  
SERVICE_REGISTRATION:
  SNAPSHOT: "META-INF/services/liquibase.snapshot.SnapshotGenerator"
  DIFF: "META-INF/services/liquibase.diff.DiffGenerator"
```

### Implementation Template
```java
// 1. Extend unified base class
public class WarehouseSnapshotGeneratorUnified extends ExtensionObjectSnapshotGenerator<Warehouse> {
    
    public WarehouseSnapshotGeneratorUnified() {
        super(Warehouse.class);
    }

    @Override
    protected Class<Warehouse> getObjectType() {
        return Warehouse.class;
    }

    @Override
    protected Class<? extends DatabaseObject>[] getParentObjectTypes() {
        return new Class[] { Account.class };  // Account-level
        // OR: return new Class[] { Schema.class }; // Schema-level
    }

    @Override
    protected String getDiscoverySQL() {
        // Account-level: SHOW commands (no parameters)
        return "SHOW WAREHOUSES";
        // Schema-level: INFORMATION_SCHEMA queries (? parameter)
        // return "SELECT * FROM INFORMATION_SCHEMA.OBJECTS WHERE SCHEMA = ?";
    }

    @Override
    protected String getObjectNameFromResultSet(ResultSet rs) throws SQLException {
        return rs.getString("name"); // or "OBJECT_NAME"
    }

    @Override
    protected Warehouse createObjectFromResultSet(ResultSet rs) throws SQLException, DatabaseException {
        Warehouse warehouse = new Warehouse();
        warehouse.setName(rs.getString("name"));
        warehouse.setSize(rs.getString("size"));
        // ... set all properties
        return warehouse;
    }
}
```

### Debugging Framework Issues
```yaml
COMPILATION_ISSUES:
  SYMPTOM: "Unreported exception DatabaseException"
  SOLUTION: "Add throws DatabaseException to method signatures"
  
  SYMPTOM: "Cannot find symbol: method"
  SOLUTION: "Check interface requirements (hash, getPriority, findDifferences)"
  
RUNTIME_ISSUES:
  SYMPTOM: "Objects not discovered"
  DEBUG: "Add System.out.println in addTo() method"
  VERIFY: "Check service registration"
  
TEMPORARY_SOLUTIONS:
  DISABLE_FILES: "mv file.java file.java.disabled"
  SIMPLE_VERSION: "Create proof-of-concept without complex patterns"
  A_B_TESTING: "Comment out in service registration with #"
```

### Migration Strategy
```yaml
PHASE_1_PROOF_OF_CONCEPT:
  - "Create simple DiffGenerator (no base classes)"
  - "Validate basic functionality"
  - "Test with existing snapshot generators"
  
PHASE_2_UNIFIED_FRAMEWORK:
  - "Create ExtensionObjectSnapshotGenerator base class"
  - "Migrate existing generators to unified pattern"
  - "Update service registrations"
  
PHASE_3_CLEANUP:
  - "Remove legacy generators"
  - "Complete unified framework"
  - "Update documentation"
```

## QUICK_REFERENCE_COMMANDS

### Architectural Assessment
```bash
# Check if object exists in Liquibase core
find . -name '*${ObjectType}*.java' -path '*/liquibase-core/*'

# Check existing snapshot/diff implementations
find . -name '*${ObjectType}*' -path '*/snapshot/*' -o -path '*/diff/*'

# Verify database object queryability
echo "SELECT * FROM INFORMATION_SCHEMA.${OBJECT_TYPE}S LIMIT 1" | sqlcmd
```

### Implementation Validation
```bash
# Quick compilation check (SESSION PATTERN)
mvn compile -q

# Targeted testing patterns (SESSION PATTERN)
mvn test -Dtest="*ObjectType*Test*" -q
mvn test -Dtest="ObjectTypeSnapshotGeneratorTest" -q
mvn test -Dtest="*ObjectType*IntegrationTest" -q

# Temporarily disable for compilation (SESSION PATTERN)
mv file.java file.java.disabled
mv file.java.disabled file.java

# Service registration management (SESSION PATTERN)
# Comment out with # for A/B testing
# Add comments for maintenance:
# # Legacy generators (will be phased out)
# # liquibase.diff.AccountLevelDiffGenerator

# Compile and verify service registration
mvn compile -q

# Check service registration
grep -r "${ObjectType}" src/main/resources/META-INF/services/

# Run specific object tests
mvn test -Dtest="*${ObjectType}*Test"

# Run integration tests
mvn test -Dtest="*${ObjectType}*IntegrationTest"

# Full test suite
mvn test

# Check for compilation errors
mvn compile 2>&1 | grep -i error
```

### Manual Database Testing
```sql
-- Test object creation and querying
CREATE ${OBJECT_TYPE} test_object_name [WITH properties];

-- Verify object appears in INFORMATION_SCHEMA
SELECT * FROM INFORMATION_SCHEMA.${OBJECT_TYPE}S 
WHERE ${name_column} = 'test_object_name';

-- Test snapshot SQL manually
[Execute your snapshot SQL query]

-- Clean up
DROP ${OBJECT_TYPE} test_object_name;
```

### Build Validation
```bash
# Compile implementation
mvn compile

# Package with tests
mvn package

# Lint check (if available)
mvn checkstyle:check
```

## SYSTEMATIC_COVERAGE_ENHANCEMENT_WORKFLOW

### Phase-Based Coverage Enhancement (95%+ Achievement Pattern)
```yaml
SYSTEMATIC_APPROACH:
  STEP_1_ASSESSMENT:
    COMMAND: "mvn test jacoco:report && open target/site/jacoco/index.html"
    ACTION: "Identify lowest coverage snapshot generators"
    TARGET: "Focus on generators with <70% coverage first"
    
  STEP_2_PRIORITIZATION:
    STRATEGY: "Target impact-to-effort ratio"
    PRIORITY_ORDER:
      - "0% coverage generators (highest impact)"
      - "Low coverage (<50%) complex generators"
      - "Medium coverage (50-80%) simple generators"
      - "High coverage (80%+) optimization only"
      
  STEP_3_ENHANCEMENT:
    PRIMARY_PATTERN: "Complete SQL String Assertions"
    COVERAGE_TARGETS:
      - "Constructor and basic method testing"
      - "Enhanced edge case coverage (null handling, large datasets)"
      - "Exception scenarios with resource cleanup verification"
      - "Advanced MockedStatic patterns for complex integrations"
      - "Reflection-based testing for protected methods"
      
  STEP_4_VALIDATION:
    COMMAND: "mvn test -Dtest=\"*{GeneratorName}*Test*\" -q"
    VERIFICATION: "All new tests pass, coverage increased significantly"
    TARGET: "95%+ overall package coverage"
    
  STEP_5_FINAL_ASSESSMENT:
    COMMAND: "mvn test jacoco:report && open target/site/jacoco/index.html"
    SUCCESS_CRITERIA: "95%+ snapshot package coverage achieved"
```

### TodoWrite Tool Integration for Multi-Phase Work
```yaml
PROGRESS_TRACKING:
  PHASE_BREAKDOWN:
    - "Phase 7: Create completely missing generators (0% coverage)"
    - "Phase 8: Enhance existing low coverage generators (<50%)"
    - "Phase 9: Optimize medium coverage generators (50-80%)"
    - "Phase 10: Systematic enhancement of all remaining generators"
    - "Phase 11: Final validation and 95%+ achievement"
    
  TODO_MANAGEMENT:
    PATTERN: "Mark tasks in_progress before starting, completed immediately after finishing"
    TRACKING: "Use TodoWrite tool for visibility into multi-step progress"
    VALIDATION: "Update todos in real-time as work progresses"
```

### Advanced MockedStatic Patterns for Complex Integration Testing
```yaml
EXECUTORSERVICE_CHAIN_MOCKING:
  PATTERN: |
    try (MockedStatic<Scope> mockedScope = mockStatic(Scope.class)) {
        // Mock the complete chain: Scope → ExecutorService → Executor
        mockedScope.when(Scope::getCurrentScope).thenReturn(scope);
        when(scope.getExecutorService()).thenReturn(executorService);
        when(executorService.getExecutor(database)).thenReturn(executor);
        when(executor.queryForList(any(RawParameterizedSql.class))).thenReturn(mockResults);
        
        // Execute test
        DatabaseObject result = generator.snapshotObject(example, databaseSnapshot);
        
        // Verify complete integration
        assertNotNull(result);
    }
    
DUAL_SQL_QUERY_PATTERN:
  USE_CASE: "For generators using SHOW commands + result_scan"
  PATTERN: |
    ArgumentCaptor<RawParameterizedSql> sqlCaptor = ArgumentCaptor.forClass(RawParameterizedSql.class);
    when(executor.queryForList(sqlCaptor.capture())).thenReturn(showResults, selectResults);
    
    // Execute test
    generator.addTo(account, snapshot);
    
    // Verify both SQL queries with complete string assertions
    List<RawParameterizedSql> capturedSQLs = sqlCaptor.getAllValues();
    assertEquals("SHOW UNIQUE KEYS IN TEST_DB.PUBLIC.TEST_TABLE", capturedSQLs.get(0).getSql());
    assertEquals("SELECT \"column_name\" AS COLUMN_NAME FROM TABLE(result_scan(last_query_id())) WHERE \"constraint_name\"= ?", capturedSQLs.get(1).getSql());
```

### Resource Management and Exception Testing Patterns
```yaml
CLEANUP_VERIFICATION_PATTERN:
  CRITICAL: "Always verify resource cleanup even in exception scenarios"
  PATTERN: |
    @Test
    void testMethod_ExceptionScenario_EnsuresResourceCleanup() {
        // Given: Setup that will throw exception
        when(resultSet.next()).thenThrow(new SQLException("Simulated error"));
        
        // When: Exception occurs during processing
        assertThrows(SQLException.class, () -> {
            generator.getDatabaseSchemaNames(database);
        });
        
        // Then: Resources should still be cleaned up
        verify(resultSet).close(); // Critical: Verify cleanup occurred
    }
    
LARGE_DATASET_TESTING:
  PURPOSE: "Test performance/memory efficiency with realistic data volumes"
  PATTERN: |
    @Test
    void testMethod_LargeDataset_HandlesEfficiently() {
        // Mock 100+ results to test performance/memory efficiency
        when(resultSet.next())
            .thenReturn(true, true, true, /* ... 100 times ... */, true)
            .thenReturn(false);
        
        String[] results = generator.getResults(database);
        assertEquals(100, results.length, "Should handle large datasets");
        verify(resultSet, times(101)).next(); // 100 results + 1 end condition
    }
```

## SUCCESS_CRITERIA

### Object Model Completeness
```yaml
OBJECT_MODEL_COMPLETE:
  STRUCTURE:
    - "Object model class with all required properties"
    - "Proper extends AbstractDatabaseObject"
    - "Correct equals/hashCode implementation (name, catalog, schema only)"
    - "Comprehensive toString for debugging"
    
  VALIDATION:
    - "validate() method with proper error handling"
    - "Property normalization methods"
    - "Appropriate constructor patterns"
```

### Snapshot Generator Completeness
```yaml
SNAPSHOT_GENERATOR_COMPLETE:
  FUNCTIONALITY:
    - "Generates complete snapshots with all properties"
    - "Handles null/default values consistently"
    - "Proper SQL query with parameter binding"
    - "Correct result set processing"
    
  INTEGRATION:
    - "Properly registered as service in META-INF/services"
    - "Correct supports() method implementation"
    - "Unit tests pass with mock data"
    - "Integration tests work with real database"
```

### Diff Comparator Completeness
```yaml
COMPARATOR_COMPLETE:
  COMPARISON_LOGIC:
    - "Compares all configuration properties"
    - "Excludes state properties from comparison"
    - "Handles edge cases (nulls, defaults, empty strings)"
    - "Consistent property normalization"
    
  TESTING:
    - "Unit tests cover all comparison scenarios"
    - "No differences for identical objects"
    - "Proper differences for changed properties"
    - "State properties properly excluded"
```

### Integration Validation
```yaml
INTEGRATION_COMPLETE:
  DATABASE_INTEGRATION:
    - "Real database objects can be snapshotted"
    - "Diff detects actual differences accurately"
    - "No unexpected differences for identical objects"
    - "Integration tests pass consistently"
    
  FRAMEWORK_INTEGRATION:
    - "Service registration working correctly"
    - "No compilation or runtime errors"
    - "Proper error handling and logging"
    - "Performance acceptable for target use cases"
```

## TROUBLESHOOTING QUICK REFERENCE

### Compilation Issues
- Check service registration files
- Verify all imports are correct
- Ensure object model extends AbstractDatabaseObject
- Confirm generator/comparator signatures match framework

### Snapshot Issues
- Test SQL queries manually in database
- Check case sensitivity of object/column names
- Verify database connection and permissions
- Validate INFORMATION_SCHEMA access

### Diff Issues
- Ensure state properties are excluded
- Check property normalization consistency
- Verify equals/hashCode implementation
- Test with identical objects first

### Integration Test Issues
- Confirm test database supports object type
- Check object creation permissions
- Verify cleanup in finally blocks
- Consider test harness limitations

## PROVEN_PATTERNS_FROM_95%_COVERAGE_ACHIEVEMENT

### Complete SQL String Assertion Success Factors
```yaml
WHY_THIS_APPROACH_SUCCEEDED:
  USER_FEEDBACK_INTEGRATION: "testing the completed SQL string is a better test"
  RELIABILITY_IMPROVEMENT: "Caught subtle formatting and ordering issues missed by component testing"
  MAINTENANCE_REDUCTION: "Single assertion point instead of multiple fragile component checks"
  REAL_WORLD_ALIGNMENT: "Tests actual SQL executed against database"
  
IMPLEMENTATION_SUCCESS:
  ADOPTION_RATE: "Immediately adopted across all subsequent test enhancements"
  COVERAGE_IMPACT: "Enabled systematic achievement of 95%+ coverage"
  TEST_RELIABILITY: "Dramatically reduced false positives and brittle tests"
  DEVELOPMENT_SPEED: "Faster test writing with clearer failure diagnostics"
```

### Advanced Integration Testing Success Factors
```yaml
MOCKEDSTATIC_MASTERY:
  COMPLEX_CHAINS: "Successfully mocked Scope→ExecutorService→Executor chains"
  DUAL_QUERIES: "Handled SHOW commands + result_scan patterns effectively"
  RESOURCE_CLEANUP: "Verified cleanup in exception scenarios"
  
EDGE_CASE_EXCELLENCE:
  NULL_HANDLING: "Comprehensive null parameter and return value testing"
  LARGE_DATASETS: "Performance testing with 100+ mock results"
  EXCEPTION_PROPAGATION: "Verified proper exception handling throughout call chains"
  REFLECTION_TESTING: "Full coverage of protected methods using reflection patterns"
```

### Systematic Enhancement Success Factors
```yaml
PHASE_BASED_APPROACH:
  TARGET_SELECTION: "Lowest coverage first for maximum impact"
  INCREMENTAL_PROGRESS: "Each generator enhanced individually with immediate validation"
  REAL_TIME_TRACKING: "TodoWrite tool provided visibility and momentum"
  
COVERAGE_ACHIEVEMENT:
  BASELINE: "Started at actual 50% coverage (not assumed 80%)"
  TARGET: "Achieved 95%+ through systematic enhancement"
  SUSTAINABILITY: "Patterns established for future generator implementations"
```

## 🔗 GUIDE INTEGRATION

**This guide covers**: Database introspection, schema comparison, extension object patterns, 95%+ test coverage workflows

**Related guides**:
- **BEFORE**: Use `CHANGETYPE_IMPLEMENTATION_GUIDE.md` for database operations (CREATE/ALTER/DROP)
- **REFERENCE**: Use `CLAUDE.md` for project status and unified framework breakthrough
- **AFTER**: Use `DIFF_GENERATE_CHANGELOG_IMPLEMENTATION_GUIDE.md` for changelog generation

**Complete workflow**: Changetype → **Snapshot/Diff** → Generate Changelog → Validation

This guide provides everything needed for complete snapshot/diff implementation with proven 95%+ coverage achievement patterns.