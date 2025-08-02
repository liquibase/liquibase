# Changetype Implementation Quick Reference
## Commands, Decision Trees, and Common Patterns

## EXECUTION_PROTOCOL
```yaml
PROTOCOL_VERSION: 2.0
DOCUMENT_TYPE: QUICK_REFERENCE
PURPOSE: "Fast reference for common changetype implementation tasks"
ADDRESSES_CORE_ISSUES: "Quick access to core issue prevention patterns"
```

## 🎯 DECISION TREE - QUICK PATH SELECTION

```yaml
QUESTION_1: "Does Liquibase have this changetype?"
  ANSWER_NO:
    PATH: "New Changetype Implementation"
    GUIDE: "changetype_patterns.md → New Changetype Pattern"
    COMPLEXITY: "HIGH"
    TIME_ESTIMATE: "4-8 hours"
    
  ANSWER_YES:
    QUESTION_2: "What needs to change?"
    
    OPTION_A_SQL_ONLY:
      PATH: "SQL Generator Override"
      GUIDE: "sql_generator_overrides.md"
      COMPLEXITY: "LOW"
      TIME_ESTIMATE: "1-2 hours"
      
    OPTION_B_ADD_ATTRIBUTES:
      PATH: "Namespace Attribute Extension" 
      GUIDE: "changetype_patterns.md → Extension Pattern"
      COMPLEXITY: "MEDIUM"
      TIME_ESTIMATE: "2-4 hours"
```

## ⚡ QUICK START COMMANDS

### Discovery Commands (ALWAYS RUN FIRST)
```bash
# Check if changetype already exists
find . -name "*[ChangeType]*" -type f

# Check for existing generators
find . -name "*[ChangeType]*Generator*.java"

# Check service registrations
ls -la src/main/resources/META-INF/services/
grep -i [changetype] src/main/resources/META-INF/services/*

# Check XSD for existing attributes
grep -i [changetype] src/main/resources/*.xsd
```

### Build and Test Commands
```bash
# Build extension (CRITICAL: use install not package)
cd liquibase-[database]
mvn clean install -DskipTests

# Run unit tests
mvn test -Dtest="*[ChangeType]*Test"

# Run test harness
cd ../liquibase-test-harness
mvn test -Dtest=ChangeObjectTests -DchangeObjects=[changeType] -DdbName=[database]
```

### Debug Commands
```bash
# Check JAR contents
jar -tf target/liquibase-*.jar | grep [ChangeType]
jar -tf target/liquibase-*.jar | grep META-INF/services

# Check component loading
mvn test -X | grep -i [ChangeType]

# Verify database objects
echo "SHOW [OBJECTS] LIKE 'TEST_%';" | database-cli
```

## 📋 IMPLEMENTATION CHECKLISTS

### New Changetype Checklist
```yaml
REQUIREMENTS:
  - [ ] "Requirements document created with official documentation"
  - [ ] "All properties documented in attribute table"
  - [ ] "5+ complete SQL examples provided"
  - [ ] "Mutual exclusivity rules identified"
  
IMPLEMENTATION:
  - [ ] "Change class with all attributes and validation"
  - [ ] "Statement class supporting all attributes"
  - [ ] "SQL generator with complete SQL generation"
  - [ ] "Service registration complete"
  
TESTING:
  - [ ] "Unit tests with complete SQL string comparison"
  - [ ] "Test harness files covering all scenarios"
  - [ ] "Integration tests passing"
  - [ ] "All property combinations tested"
```

### SQL Generator Override Checklist
```yaml
RESEARCH:
  - [ ] "Database-specific SQL syntax documented"
  - [ ] "Differences from standard SQL identified"
  - [ ] "All syntax variations researched"
  
IMPLEMENTATION:
  - [ ] "Generator class with higher priority"
  - [ ] "Complete SQL generation with all attributes"
  - [ ] "SQL validation for completeness"
  - [ ] "Service registration added"
  
TESTING:
  - [ ] "Unit tests with exact SQL string comparison"
  - [ ] "All property combinations tested"
  - [ ] "Test harness files created"
  - [ ] "Integration tests covering all SQL scenarios"
```

### Extension Pattern Checklist
```yaml
DISCOVERY:
  - [ ] "Existing Storage, Parser, Generator identified"
  - [ ] "Service registrations documented"
  - [ ] "XSD attributes reviewed"
  
IMPLEMENTATION:
  - [ ] "Namespace attribute storage enhanced"
  - [ ] "Parser updated with higher priority"
  - [ ] "SQL generator handles namespace attributes"
  - [ ] "Service registrations updated"
  
TESTING:
  - [ ] "Unit tests include namespace attribute scenarios"
  - [ ] "Test harness covers all attribute combinations"
  - [ ] "SQL generation with attributes verified"
  - [ ] "Integration tests comprehensive"
```

## 🔧 COMMON PATTERNS AND TEMPLATES

### File Location Patterns
```yaml
CHANGE_CLASSES:
  PATH: "src/main/java/liquibase/change/core/[ChangeType]Change.java"
  PACKAGE: "liquibase.change.core"
  
STATEMENT_CLASSES:
  PATH: "src/main/java/liquibase/statement/core/[ChangeType]Statement.java"
  PACKAGE: "liquibase.statement.core"
  
SQL_GENERATORS:
  PATH: "src/main/java/liquibase/sqlgenerator/core/[ChangeType]Generator[Database].java"
  PACKAGE: "liquibase.sqlgenerator.core"
  
SERVICE_FILES:
  CHANGE: "src/main/resources/META-INF/services/liquibase.change.Change"
  GENERATOR: "src/main/resources/META-INF/services/liquibase.sqlgenerator.SqlGenerator"
  
TEST_HARNESS:
  CHANGELOG: "test-harness/src/main/resources/liquibase/harness/change/changelogs/[database]/[changeType].xml"
  EXPECTED_SQL: "test-harness/src/main/resources/liquibase/harness/change/expectedSql/[database]/[changeType].sql"
  EXPECTED_SNAPSHOT: "test-harness/src/main/resources/liquibase/harness/change/expectedSnapshot/[changeType].json"
```

### Priority Patterns
```yaml
GENERATOR_PRIORITIES:
  DEFAULT: "PRIORITY_DEFAULT = 1"
  DATABASE_SPECIFIC: "PRIORITY_DATABASE = 5"
  OVERRIDE: "PRIORITY_DATABASE + 5 = 10"
  PARSER_OVERRIDE: "PRIORITY_DATABASE + 10 = 15"
  
RULE: "Higher priority = used first"
```

### Validation Patterns
```yaml
CHANGE_VALIDATION:
  REQUIRED_FIELDS: "Check for null/empty required attributes"
  MUTUAL_EXCLUSIVITY: "Check conflicting attribute combinations"
  VALUE_CONSTRAINTS: "Check ranges, formats, valid values"
  BUSINESS_RULES: "Check database-specific constraints"
  
GENERATOR_VALIDATION:
  STATEMENT_COMPLETENESS: "Verify all required attributes present"
  SQL_COMPLETENESS: "Verify generated SQL is complete and valid"
  
UNIT_TEST_VALIDATION:
  COMPLETE_SQL_COMPARISON: "assertEquals(expectedSQL, actualSQL)"
  ALL_COMBINATIONS: "Test every property combination"
  EDGE_CASES: "Test boundary conditions and error cases"
```

## 🚨 CRITICAL ERROR PATTERNS - QUICK FIXES

### "No generator found for type [ObjectType]"
```bash
# QUICK FIX
# 1. Check service registration
cat src/main/resources/META-INF/services/liquibase.sqlgenerator.SqlGenerator | grep [ObjectType]

# 2. Add if missing
echo "liquibase.sqlgenerator.core.[ObjectType]Generator[Database]" >> src/main/resources/META-INF/services/liquibase.sqlgenerator.SqlGenerator

# 3. Rebuild
mvn clean install -DskipTests
```

### "Database is up to date"
```xml
<!-- QUICK FIX: Add to test XML -->
<changeSet id="cleanup-[object]" author="test-harness" runAlways="true">
    <sql>DROP [OBJECT_TYPE] IF EXISTS TEST_[OBJECT] CASCADE;</sql>
</changeSet>
```

### "Expected SQL doesn't match"
```bash
# QUICK FIX
# 1. Check actual generated SQL
mvn test -Dtest=[ChangeType]GeneratorTest -Dlogback.configurationFile=logback-debug.xml

# 2. Update expected SQL to match exactly
# 3. Ensure complete SQL string comparison in tests
assertEquals("COMPLETE_EXPECTED_SQL", actualSQL);
```

### "Generator not being used"
```java
// QUICK FIX: Check priority
@Override
public int getPriority() {
    return PRIORITY_DATABASE + 5;  // Must be higher than default
}

@Override  
public boolean supports([ChangeType]Statement statement, Database database) {
    return database instanceof [Database]Database;  // Must match target database
}
```

## 📊 TESTING PATTERNS

### Unit Test Template (Complete SQL Comparison)
```java
@Test
public void testGeneratesCompleteSql() {
    // Arrange
    [ChangeType]Statement statement = new [ChangeType]Statement();
    statement.setRequiredAttribute("TEST_VALUE");
    
    // Act
    Sql[] sqls = generator.generateSql(statement, database, null);
    
    // Assert - CRITICAL: Complete SQL string comparison
    String actualSQL = sqls[0].toSql();
    String expectedSQL = "COMPLETE_EXPECTED_SQL_WITH_ALL_ELEMENTS";
    assertEquals(expectedSQL, actualSQL, "SQL must match exactly");
    
    // Verify completeness
    assertTrue(actualSQL.contains("REQUIRED_KEYWORD"));
    assertTrue(actualSQL.contains("TEST_VALUE"));
}
```

### Test Harness XML Template
```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                   xmlns:[database]="http://www.liquibase.org/xml/ns/[database]"
                   xsi:schemaLocation="...">

    <!-- Test basic functionality -->
    <changeSet id="test-basic-[changeType]" author="test-harness">
        <[database]:[changeType] requiredAttribute="TEST_BASIC"/>
    </changeSet>

    <!-- Test with optional attributes -->
    <changeSet id="test-optional-[changeType]" author="test-harness">
        <[database]:[changeType] requiredAttribute="TEST_OPTIONAL"
                                 optionalAttribute="OPTIONAL_VALUE"/>
    </changeSet>

    <!-- Test edge cases -->
    <changeSet id="test-edge-[changeType]" author="test-harness">
        <[database]:[changeType] requiredAttribute="TEST_EDGE"
                                 optionalAttribute="BOUNDARY_VALUE"/>
    </changeSet>

</databaseChangeLog>
```

## 🔍 DEBUGGING QUICK REFERENCE

### 5-Layer Debugging Framework
```yaml
LAYER_1_CODE_LOADING:
  CHECK: "jar -tf target/*.jar | grep [ChangeType]"
  CHECK: "jar -tf target/*.jar | grep META-INF/services"
  
LAYER_2_REGISTRATION:
  CHECK: "mvn test -X | grep -i [ChangeType]"
  CHECK: "Look for component loading messages"
  
LAYER_3_EXECUTION:
  CHECK: "Add debug logging to methods"
  CHECK: "Verify method parameters"
  
LAYER_4_DATA_CREATION:
  CHECK: "Manual database query: SHOW [OBJECTS] LIKE 'TEST_%'"
  CHECK: "Verify expected database state"
  
LAYER_5_TEST_FRAMEWORK:
  CHECK: "Verify test harness configuration"
  CHECK: "Check JSON format expectations"
```

### Common SQL Syntax Patterns
```yaml
SNOWFLAKE_PATTERNS:
  ADD_COLUMN: "ALTER TABLE t ADD COLUMN c TYPE"
  DROP_COLUMN: "ALTER TABLE t DROP COLUMN c"
  RENAME_COLUMN: "ALTER TABLE t RENAME COLUMN old TO new"
  
POSTGRES_PATTERNS:
  ADD_COLUMN: "ALTER TABLE t ADD COLUMN c TYPE"
  DROP_COLUMN: "ALTER TABLE t DROP COLUMN c"
  RENAME_COLUMN: "ALTER TABLE t RENAME COLUMN old TO new"
  
ORACLE_PATTERNS:
  ADD_COLUMN: "ALTER TABLE t ADD (c TYPE)"
  DROP_COLUMN: "ALTER TABLE t DROP COLUMN c"
  RENAME_COLUMN: "ALTER TABLE t RENAME COLUMN old TO new"
```

## 🚀 AUTOMATION SCRIPTS

### One-Command Workflows
```bash
# New changetype complete workflow
./scripts/new-changetype-workflow.sh [database] [changetype]

# SQL generator override workflow  
./scripts/sql-generator-workflow.sh [database] [changetype] [operation]

# Extension workflow
./scripts/extend-changetype-workflow.sh [changetype] [database] [attributes]

# Test harness workflow
./scripts/test-harness-workflow.sh [database] [changetype]

# Requirements validation
./scripts/validate-requirements.sh [changetype]
```

## 📚 REFERENCE LINKS

### Internal Documentation
```yaml
MASTER_PROCESS: "master_process_loop.md"
IMPLEMENTATION_PATTERNS: "changetype_patterns.md"
SQL_OVERRIDES: "sql_generator_overrides.md"
TEST_HARNESS: "test_harness_guide.md"
REQUIREMENTS: "requirements_creation.md"
NAVIGATION: "README.md"
```

### Error Resolution
```yaml
ERROR_PATTERNS: "../snapshot_diff_implementation/error_patterns_guide.md"
SYSTEMATIC_DEBUGGING: "../snapshot_diff_implementation/main_guide.md"
```

## 🎯 SUCCESS METRICS QUICK CHECK

### Implementation Complete When:
- [ ] All unit tests pass with complete SQL string comparison
- [ ] Test harness passes with comprehensive coverage
- [ ] All property combinations tested  
- [ ] Requirements fully covered
- [ ] Service registration complete
- [ ] Documentation updated

### Quality Gates:
- [ ] **Requirements**: Complete with official documentation
- [ ] **Implementation**: All classes with proper validation
- [ ] **Testing**: Unit + integration tests comprehensive
- [ ] **Validation**: All core issues addressed

Remember: Use this as a quick reference, but always follow the complete processes in the main guides for best results!