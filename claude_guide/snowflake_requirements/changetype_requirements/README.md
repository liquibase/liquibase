# Changetype Requirements Navigation
## Complete Requirements Documentation for Snowflake Changetype Implementation

## FOLDER_OVERVIEW
```yaml
PURPOSE: "Requirements for implementing Snowflake-specific changetypes and extensions"
ORGANIZATION: "New changetypes vs existing changetype extensions"
LAST_UPDATED: "2025-08-01"
SUPPORTS_IMPLEMENTATION_GUIDES: "../../../implementation_guides/changetype_implementation/"
```

## 🎯 QUICK NAVIGATION BY IMPLEMENTATION PATTERN

### New Changetype Implementation (Objects not in core Liquibase)
**GO TO**: `new_changetypes/`
```yaml
PATTERN: "New Changetype Pattern"  
IMPLEMENTATION_GUIDE: "../../../implementation_guides/changetype_implementation/changetype_patterns.md"
REQUIREMENTS_INCLUDE:
  - "Complete Snowflake object lifecycle (create/alter/drop)"
  - "All official parameters and syntax variations"
  - "Comprehensive SQL examples and test scenarios"
  - "Service registration requirements"
```

**Available Requirements:**
- `createWarehouse_requirements.md` ✅ UPDATED with RESOURCE_CONSTRAINT
- `alterWarehouse_requirements.md`
- `dropWarehouse_requirements.md`
- `createDatabase_requirements.md`
- `alterDatabase_requirements.md`
- `dropDatabase_requirements.md`

### Existing Changetype Extension (Adding Snowflake attributes to core changetypes)
**GO TO**: `existing_changetype_extensions/`
```yaml
PATTERN: "Extension Pattern or SQL Override Pattern"
IMPLEMENTATION_GUIDE: "../../../implementation_guides/changetype_implementation/changetype_patterns.md"
SQL_OVERRIDE_GUIDE: "../../../implementation_guides/changetype_implementation/sql_generator_overrides.md"
REQUIREMENTS_INCLUDE:
  - "Snowflake-specific attributes analysis"
  - "Priority classification (HIGH/MEDIUM/LOW)"
  - "Integration strategy with existing Liquibase changetype"
  - "Namespace attribute mapping"
```

**Available Requirements:**
- **Schema Operations:**
  - `createSchema_requirements.md`
  - `alterSchema_requirements.md`
  - `dropSchema_requirements.md`
- **Table Operations:**
  - `alterTable_requirements.md`
  - `createTableEnhanced_requirements.md` (namespace attributes)
  - `alterTableEnhanced_requirements.md` (namespace attributes)
  - `dropTableEnhanced_requirements.md` (namespace attributes)
  - `ALTERTABLE_NAMESPACE_REQUIREMENTS_UPDATED.md`
- **Sequence Operations:**
  - `createSequenceEnhanced_requirements.md` (namespace attributes)
  - `alterSequenceEnhanced_requirements.md` (namespace attributes)
  - `dropSequenceEnhanced_requirements.md` (namespace attributes)

## 🔍 FINDING THE RIGHT REQUIREMENTS

### By Snowflake Object Type
```yaml
WAREHOUSES: "new_changetypes/[create|alter|drop]Warehouse_requirements.md"
DATABASES: "new_changetypes/[create|alter|drop]Database_requirements.md"
SCHEMAS: "existing_changetype_extensions/[create|alter|drop]Schema_requirements.md"
TABLES: "existing_changetype_extensions/[create|alter|drop]Table*_requirements.md"
SEQUENCES: "existing_changetype_extensions/[create|alter|drop]Sequence*_requirements.md"
```

### By Implementation Need
```yaml
COMPLETELY_NEW_OBJECT:
  FOLDER: "new_changetypes/"
  EXAMPLE: "Warehouses don't exist in core Liquibase"
  
ADD_SNOWFLAKE_ATTRIBUTES:
  FOLDER: "existing_changetype_extensions/"
  EXAMPLE: "Add 'transient' attribute to createTable"
  
OVERRIDE_SQL_GENERATION:
  FOLDER: "existing_changetype_extensions/"
  EXAMPLE: "Generate Snowflake-specific SQL for existing changetype"
```

## 📊 REQUIREMENTS QUALITY STANDARDS

### High-Quality Requirements Include:
```yaml
OFFICIAL_DOCUMENTATION:
  - "Snowflake documentation URLs with specific versions"
  - "Complete parameter documentation from official source"
  
COMPLETE_SYNTAX_COVERAGE:
  - "All SQL syntax variations documented"
  - "Optional clauses and parameters identified"
  - "Mutual exclusivity rules documented"
  
COMPREHENSIVE_EXAMPLES:
  - "5+ complete SQL examples covering all scenarios"
  - "Edge cases and boundary conditions"
  - "Examples demonstrating all parameter combinations"
  
TEST_SCENARIO_PLANNING:
  - "Test scenarios for all SQL generation paths"
  - "Separate test files for mutually exclusive features"
  - "Integration test coverage requirements"
  
VALIDATION_REQUIREMENTS:
  - "All validation rules with error messages"
  - "Input validation and constraint checking"
  - "Database compatibility requirements"
```

## 🚀 USING THESE REQUIREMENTS

### Step-by-Step Process
1. **Identify Implementation Pattern**
   - New object not in Liquibase core? → `new_changetypes/`
   - Adding attributes to existing changetype? → `existing_changetype_extensions/`

2. **Find Specific Requirements**
   - Navigate to appropriate subfolder
   - Open the `[changetype]_requirements.md` file
   - Review complete requirements document

3. **Follow Implementation Guide**
   - Use `../../../implementation_guides/changetype_implementation/changetype_patterns.md`
   - For SQL overrides: use `sql_generator_overrides.md`
   - Follow sequential blocking execution protocols

4. **Test Implementation**
   - Use `../../../implementation_guides/changetype_implementation/test_harness_guide.md`
   - Ensure comprehensive coverage of all documented scenarios

## 📋 REQUIREMENTS CHECKLIST

### Before Starting Implementation
- [ ] Requirements document exists and is complete
- [ ] Official Snowflake documentation references verified
- [ ] All SQL syntax variations documented
- [ ] Test scenarios planned and documented
- [ ] Validation rules identified
- [ ] Implementation pattern chosen (New vs Extension vs SQL Override)

### During Implementation
- [ ] All documented parameters implemented
- [ ] All SQL examples tested
- [ ] Validation rules implemented with documented error messages
- [ ] Integration tests cover all documented scenarios
- [ ] Unit tests compare complete SQL strings exactly

### After Implementation
- [ ] All documented test scenarios pass
- [ ] Edge cases handled as documented
- [ ] Error conditions produce documented error messages
- [ ] Requirements updated based on implementation learnings

## 🔗 CROSS-REFERENCES

### Implementation Guides
```yaml
MASTER_PROCESS: "../../../implementation_guides/changetype_implementation/master_process_loop.md"
CHANGETYPE_PATTERNS: "../../../implementation_guides/changetype_implementation/changetype_patterns.md"
SQL_OVERRIDES: "../../../implementation_guides/changetype_implementation/sql_generator_overrides.md"
TEST_HARNESS: "../../../implementation_guides/changetype_implementation/test_harness_guide.md"
QUICK_REFERENCE: "../../../implementation_guides/changetype_implementation/quick_reference.md"
```

### Parent Navigation
```yaml
MAIN_REQUIREMENTS: "../README.md"
SNAPSHOT_DIFF_REQUIREMENTS: "../snapshot_diff_requirements/README.md"
DOCUMENTATION: "../documentation/"
```

## 💡 COMMON IMPLEMENTATION PATHS

### Path 1: New Warehouse Changetype
1. Read `new_changetypes/createWarehouse_requirements.md`
2. Follow New Changetype Pattern in implementation guides
3. Implement change class, statement class, SQL generator
4. Register services and update XSD
5. Write comprehensive tests as documented

### Path 2: Add Transient Attribute to createTable
1. Read `existing_changetype_extensions/createTableEnhanced_requirements.md`
2. Choose Extension Pattern or SQL Override Pattern
3. Follow appropriate implementation guide
4. Test all documented attribute combinations

### Path 3: Override SQL Generation for alterSequence
1. Read `existing_changetype_extensions/alterSequenceEnhanced_requirements.md`
2. Follow SQL Override Pattern in implementation guides
3. Implement SQL generator override
4. Test complete SQL string generation as documented

Remember: These requirements are designed to address the four core issues that cause incomplete implementations. They provide the foundation for comprehensive, correct Snowflake extension development.