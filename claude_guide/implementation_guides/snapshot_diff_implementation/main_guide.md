# Liquibase 4.33.0 Snapshot and Diff Implementation Guide - Main Document
## Updated with Retrospective Learnings and AI-Optimization

## EXECUTION_PROTOCOL
```yaml
PROTOCOL_VERSION: 2.0
DOCUMENT_TYPE: MAIN_OVERVIEW
EXECUTION_MODE: REFERENCE_GUIDE
VALIDATION_MODE: STRICT
ADDRESSES_CORE_ISSUES: 
  - "Complete syntax definition"
  - "Complete SQL test statements" 
  - "Unit tests complete string comparison"
  - "Integration tests ALL generated SQL"
```

## DOCUMENT_NAVIGATION
```yaml
CURRENT_LOCATION: "claude_guide/implementation_guides/snapshot_diff_implementation/main_guide.md"
FOLDER_STRUCTURE:
  OVERVIEW_DOCUMENTS:
    - main_guide.md: "This document - overview, prerequisites, common pitfalls"
    - ai_quickstart.md: "Sequential execution guide with validation checkpoints"
    - README.md: "Navigation guide for all documents"
  
  IMPLEMENTATION_GUIDES:
    - part1_object_model.md: "Database object model implementation"
    - part2_snapshot_implementation.md: "SnapshotGenerator patterns and SQL"
    - part3_diff_implementation.md: "Comparator and DiffGenerator implementation"
    - part4_testing_guide.md: "Testing with harness limitations"
    - part5_reference_implementation.md: "Complete Snowflake Warehouse example"
  
  DEBUGGING_SUPPORT:
    - error_patterns_guide.md: "Comprehensive error pattern library"

COMPANION_CHANGETYPE_GUIDES:
  BASE_PATH: "../changetype_implementation/"
  KEY_DOCUMENTS:
    - master_process_loop.md: "Overall development process"
    - requirements_creation.md: "Detailed requirements specification"
```

This guide provides a comprehensive, test-driven development (TDD) approach to implementing snapshot and diff capabilities for new database objects in Liquibase 4.33.0 extensions.

## ⚠️ CRITICAL UPDATES FROM FIELD EXPERIENCE
Based on successful implementations, this guide now includes:
- **Test harness scope limitations and workarounds** → Prevents unrealistic success criteria
- **Common error patterns and solutions** → Systematic debugging framework
- **Build process optimizations** → Automated workflow scripts
- **Framework behavior verification steps** → Validation checkpoints
- **AI-optimized structure** → Sequential blocking execution protocols
- **Complete SQL testing requirements** → Addresses all four core issues

## Document Structure

This implementation guide is split into the following documents:

1. **main_guide.md** (this document) - Overview, prerequisites, common pitfalls, and solutions
2. **part1_object_model.md** - Research, requirements, and object model implementation
3. **part2_snapshot_implementation.md** - SnapshotGenerator implementation and testing
4. **part3_diff_implementation.md** - Comparator and DiffGenerator implementation
5. **part4_testing_guide.md** - Comprehensive testing strategies including test harness limitations
6. **part5_reference_implementation.md** - Complete Snowflake Warehouse example
7. **ai_quickstart.md** - Sequential execution guide with validation checkpoints
8. **error_patterns_guide.md** - Comprehensive error pattern library and systematic debugging

## Version Compatibility

This guide is specifically written for **Liquibase 4.33.0** and uses the following key APIs:
- `AbstractDatabaseObject` for database object modeling
- `SnapshotGenerator` for capturing database state
- `DatabaseObjectComparator` for object comparison
- `MissingObjectChangeGenerator`, `UnexpectedObjectChangeGenerator`, and `ChangedObjectChangeGenerator` for diff output

## Prerequisites

```yaml
TECHNICAL_REQUIREMENTS:
  - LIQUIBASE_VERSION: "4.33.0 core libraries"
  - PROJECT_SETUP: "Existing Liquibase database extension project"
  - JAVA_VERSION: "8 or higher"
  - BUILD_SYSTEM: "Gradle or Maven"
  - TEST_INFRASTRUCTURE:
      - "Unit test framework (Spock/Groovy recommended)"
      - "Integration test infrastructure"
      - "Test harness infrastructure (with limitation understanding)"
  - DATABASE_ACCESS: "Target database for testing"

PROCESS_REQUIREMENTS:
  - DOCUMENTATION_FIRST: "Complete requirements before implementation"
  - TEST_DRIVEN_DEVELOPMENT: "Unit tests before implementation"
  - SYSTEMATIC_DEBUGGING: "5-layer analysis framework"
  - REALISTIC_SUCCESS_CRITERIA: "Based on framework capabilities"
```

## Build Process Optimization

**NEW**: Automated workflow script to reduce friction and prevent manual errors:

```bash
#!/bin/bash
# scripts/snapshot-diff-workflow.sh - Enhanced with error checking
set -e

echo "=== Liquibase Snapshot/Diff Development Workflow ==="

# Validate inputs
if [ -z "$1" ] || [ -z "$2" ]; then
    echo "Usage: $0 [database] [testname]"
    echo "Example: $0 snowflake createWarehouse"
    exit 1
fi

DATABASE=$1
TESTNAME=$2

echo "1. Building extension for $DATABASE..."
cd liquibase-$DATABASE
mvn clean install -DskipTests

echo "2. Installing to local repository..."
echo "   (Required for test harness dependency resolution)"

echo "3. Changing to test harness..."
cd ../liquibase-test-harness

echo "4. Running snapshot test harness for $TESTNAME..."
mvn test -Dtest=SnapshotObjectTests -DdbName=$DATABASE -DsnapshotObjects="$TESTNAME"

echo "5. Build and test cycle complete!"
echo "=== Workflow Complete ==="
```

**Usage**: `./scripts/snapshot-diff-workflow.sh [database] [testname]`
**Benefit**: Single command to build, install, and test with proper error handling

## Common Pitfalls and Solutions

### 1. Test Harness Scope Limitations ⚠️ CRITICAL
```yaml
ISSUE: "Custom database objects may not be included in snapshot scope"
ROOT_CAUSE: "Test harness uses default snapshot scope which excludes custom objects"
IMPACT: "Tests fail for wrong reasons, causing goalpost changing"
SOLUTION:
  - VERIFY_SCOPE_FIRST: "Use Step 6.0 in ai_quickstart.md"
  - SET_REALISTIC_CRITERIA: "Changeset execution = success if not in scope"
  - DOCUMENT_LIMITATIONS: "Create test_harness_scope.md file"
REFERENCE: "part4_testing_guide.md - Test Harness CRITICAL LIMITATIONS"
PREVENTS_GOALPOST_CHANGING: "Establishes criteria before implementation"
ADDRESSES_CORE_ISSUE: "Realistic success criteria based on framework limitations"
```

### 2. Service Registration
```yaml
ISSUE: "Components not discovered by Liquibase"
SYMPTOMS:
  - "No snapshot generator found for type [ObjectType]"
  - "Cannot find implementation of [Interface]"
ROOT_CAUSE: "Missing or incorrect META-INF/services registration"
SOLUTION:
  - VERIFY_SERVICE_FILES: "Check META-INF/services files exist"
  - EXACT_CLASS_NAMES: "Ensure package.ClassName matches exactly"
  - CHECK_JAR_CONTENTS: "jar -tf target/*.jar | grep META-INF"
REFERENCE: "error_patterns_guide.md - Pattern #1"
ADDRESSES_CORE_ISSUE: "Complete syntax definition requires proper registration"
```

### 3. Database State Persistence
```yaml
ISSUE: "Changesets not re-executing in test harness"
SYMPTOMS:
  - "ChangeSet [id] has already been executed"
  - "Database is up to date, no changesets to execute"
ROOT_CAUSE: "DATABASECHANGELOG table retains execution history"
SOLUTION:
  - CLEANUP_CHANGESETS: 'Add changesets with runAlways="true"'
  - CLEAR_CHANGELOG: "Clear DATABASECHANGELOG between test runs"
REFERENCE: "error_patterns_guide.md - Pattern #2"
ADDRESSES_CORE_ISSUE: "Integration tests testing ALL generated SQL"
```

### 4. JSON Format Mismatches
```yaml
ISSUE: "Test harness expects specific JSON structure"
SYMPTOMS:
  - "JSONAssert mismatch"
  - "Unexpected fields in snapshot"
ROOT_CAUSE: "Database metadata included, IGNORE values present"
SOLUTION:
  - REMOVE_METADATA: "No database metadata fields"
  - NO_IGNORE_VALUES: "Remove all IGNORE values"
  - MATCH_WORKING_FORMAT: "Use exact format from working examples"
REFERENCE: "error_patterns_guide.md - Pattern #5"
ADDRESSES_CORE_ISSUE: "Unit tests comparing complete SQL strings"
```

### 5. Incomplete SQL Testing ⚠️ NEW
```yaml
ISSUE: "Unit tests pass but don't validate complete SQL"
SYMPTOMS:
  - "Tests pass with partial SQL strings"
  - "Generated SQL missing required clauses"
ROOT_CAUSE: "Unit tests not comparing complete SQL strings"
SOLUTION:
  - COMPLETE_STRING_COMPARISON: "Assert entire SQL statement matches"
  - ALL_PROPERTY_COMBINATIONS: "Test every property combination"
  - EXACT_SYNTAX_MATCHING: "Verify SQL matches requirements exactly"
REFERENCE: "ai_quickstart.md - Step validations"
ADDRESSES_CORE_ISSUE: "Unit tests not comparing complete SQL strings"
```

### 6. Incomplete Integration Testing ⚠️ NEW
```yaml
ISSUE: "Integration tests don't verify all generated SQL"
SYMPTOMS:
  - "Some SQL statements never executed"
  - "Database state doesn't match all property changes"
ROOT_CAUSE: "Integration tests not testing ALL generated SQL"
SOLUTION:
  - TEST_ALL_CHANGESETS: "Verify every changeset executes"
  - DATABASE_STATE_VERIFICATION: "Check final state matches all properties"
  - COMPREHENSIVE_TEST_MATRIX: "Cover all property combinations"
REFERENCE: "part4_testing_guide.md - Integration Test Requirements"
ADDRESSES_CORE_ISSUE: "Integration tests not testing ALL generated SQL"
```

## Systematic Debugging Framework

When tests fail, use this 5-layer approach to prevent goalpost changing:

```yaml
LAYER_1_CODE_LOADING:
  QUESTION: "Is the JAR built correctly? Are service files included?"
  VALIDATION:
    - "jar -tf target/*.jar | grep META-INF/services"
    - "Check service file contents match class names exactly"
  ADDRESSES_CORE_ISSUE: "Complete syntax definition"
  
LAYER_2_REGISTRATION:
  QUESTION: "Is Liquibase finding your components?"
  VALIDATION:
    - "Look for component loading messages in logs"
    - "Debug registration priority methods"
  ADDRESSES_CORE_ISSUE: "Complete syntax definition"
  
LAYER_3_EXECUTION:
  QUESTION: "Are your methods being called?"
  VALIDATION:
    - "Add debug logging to all implemented methods"
    - "Verify execution flow matches expectations"
  ADDRESSES_CORE_ISSUE: "Unit tests comparing complete SQL strings"
  
LAYER_4_DATA_CREATION:
  QUESTION: "Is data being created in the database?"
  VALIDATION:
    - "Manual query: SHOW [OBJECTS] LIKE 'TEST_%'"
    - "Verify database state matches expected"
  ADDRESSES_CORE_ISSUE: "Integration tests testing ALL generated SQL"
  
LAYER_5_TEST_FRAMEWORK:
  QUESTION: "Is test framework configured correctly?"
  VALIDATION:
    - "Check JSON format matches expectations"
    - "Verify snapshot scope includes custom objects"
  ADDRESSES_CORE_ISSUE: "Realistic success criteria"

CRITICAL_RULE: "Apply layers in order. Fix current layer before proceeding."
PREVENT_GOALPOST_CHANGING: "Don't redefine requirements to fit current code"
```

## Key Liquibase 4.33.0 APIs

### AbstractDatabaseObject
The base class for all database objects. Key methods:
```java
// REQUIRED: Define unique identifier field(s)
setSnapshotId(String); // Single field: "name"
setSnapshotId(String); // Composite: "schema,name"

// Generic attribute storage
getAttribute(String, Class);
setAttribute(String, Object);

// Standard naming interface  
getName() / setName(String);

// Define object hierarchy
getContainingObjects();
```

### SnapshotGenerator Priority System
```java
public static final int PRIORITY_NONE = -1;
public static final int PRIORITY_DEFAULT = 1;
public static final int PRIORITY_DATABASE = 5;
public static final int PRIORITY_ADDITIONAL = 50;
```

### DatabaseObjectComparator Methods
```java
// Determine if comparator handles this object type
getPriority(Class, Database);

// Identity comparison - are these the same object?
isSameObject(DatabaseObject, DatabaseObject, Database, Chain);

// Detailed comparison - what changed?
findDifferences(DatabaseObject, DatabaseObject, Database, CompareControl, Chain, Set);
```

## Service Registration

For Liquibase 4.33.0 to discover your extensions, you must register them in the appropriate service files:

### META-INF/services/liquibase.structure.DatabaseObject
```
liquibase.ext.[database].database.object.[ObjectType]
```

**⚠️ CRITICAL**: This registration is required for objects to be included in default snapshots. Without this, your custom objects will not appear in snapshot scope automatically.

### META-INF/services/liquibase.snapshot.SnapshotGenerator
```
liquibase.ext.[database].snapshot.[ObjectType]SnapshotGenerator
```

### META-INF/services/liquibase.diff.compare.DatabaseObjectComparator
```
liquibase.ext.[database].diff.output.[ObjectType]Comparator
```

### META-INF/services/liquibase.diff.output.changelog.ChangeGenerator
```
liquibase.ext.[database].diff.output.[ObjectType]DiffGenerator
```

## Development Workflow

```yaml
PHASE_1_RESEARCH:
  STEP: "Document all properties and behaviors of database object"
  VALIDATION: "Complete requirements document with all properties"
  ADDRESSES_CORE_ISSUE: "Complete syntax definition"
  
PHASE_2_MODEL:
  STEP: "Create DatabaseObject class with all properties"
  VALIDATION: "Unit tests for all property getters/setters"
  ADDRESSES_CORE_ISSUE: "Complete syntax definition"
  
PHASE_3_SNAPSHOT:
  STEP: "Implement SnapshotGenerator to read from database"
  VALIDATION: "Unit tests with complete SQL string comparison"
  ADDRESSES_CORE_ISSUE: "Unit tests comparing complete SQL strings"
  
PHASE_4_COMPARE:
  STEP: "Implement Comparator to identify differences"
  VALIDATION: "Unit tests for all comparison scenarios"
  ADDRESSES_CORE_ISSUE: "Unit tests comparing complete SQL strings"
  
PHASE_5_GENERATE:
  STEP: "Implement DiffGenerator to create change objects"
  VALIDATION: "Unit tests for all change generation scenarios"
  ADDRESSES_CORE_ISSUE: "Unit tests comparing complete SQL strings"
  
PHASE_6_TEST:
  STEP: "Integration and harness testing"
  VALIDATION: "All generated SQL verified in integration tests"
  ADDRESSES_CORE_ISSUE: "Integration tests testing ALL generated SQL"
  
PHASE_7_DEBUG:
  STEP: "Systematic debugging when issues arise"
  VALIDATION: "5-layer debugging framework applied"
  ADDRESSES_CORE_ISSUE: "Realistic success criteria"
```

## Testing Strategy

This guide emphasizes Test-Driven Development (TDD) with complete validation:

```yaml
UNIT_TESTING:
  PRINCIPLE: "Write tests before implementation"
  REQUIREMENT: "Complete SQL string comparison in all tests"
  VALIDATION: "Assert entire SQL statement matches requirements exactly"
  ADDRESSES_CORE_ISSUE: "Unit tests not comparing complete SQL strings"
  
INTEGRATION_TESTING:
  PRINCIPLE: "Test with real databases"
  REQUIREMENT: "Verify ALL generated SQL executes successfully"
  VALIDATION: "Check database state matches all property changes"
  ADDRESSES_CORE_ISSUE: "Integration tests not testing ALL generated SQL"
  
TEST_HARNESS:
  PRINCIPLE: "Use harness with understanding of limitations"
  REQUIREMENT: "Set realistic success criteria based on scope"
  VALIDATION: "Document scope limitations and adjust expectations"
  ADDRESSES_CORE_ISSUE: "Realistic success criteria"
  
EDGE_CASES:
  PRINCIPLE: "Include comprehensive negative testing"
  REQUIREMENT: "Test all property combinations and invalid inputs"
  VALIDATION: "Verify error handling and boundary conditions"
  
PERFORMANCE_TESTING:
  PRINCIPLE: "Test with realistic data volumes"
  REQUIREMENT: "Verify performance under load"
  VALIDATION: "Document performance characteristics"
```

## Test Commands Quick Reference

**Change Object Tests:**
```bash
mvn test -Dtest=ChangeObjectTests -DdbName=[database] -DchangeObjects=[testName]
```

**Snapshot Object Tests:**
```bash
mvn test -Dtest=SnapshotObjectTests -DdbName=[database] -DsnapshotObjects=[testName]
```

**Key Difference**: ChangeObjectTests use `-DchangeObjects`, SnapshotObjectTests use `-DsnapshotObjects`

## Quick Reference

```java
// Database Object - Complete Implementation Pattern
public class MyObject extends AbstractDatabaseObject {
    public MyObject() {
        setSnapshotId("name"); // or composite: "schema,name"
    }
    
    // REQUIRED: All properties must have getters/setters
    // REQUIRED: Properties categorized (Required/Optional/State)
    // REQUIRED: Only configuration properties in comparisons
}

// Snapshot Generator - Complete Implementation Pattern
public class MyObjectSnapshotGenerator extends SnapshotGenerator {
    @Override
    public int getPriority(Class<?> objectType, Database database) {
        return database instanceof MyDatabase && 
               MyObject.class.isAssignableFrom(objectType) 
               ? PRIORITY_DATABASE : PRIORITY_NONE;
    }
    
    // REQUIRED: Complete SQL query implementation
    // REQUIRED: ResultSet parsing for all properties
    // REQUIRED: Unit tests with complete SQL string comparison
}

// Comparator - Complete Implementation Pattern
public class MyObjectComparator implements DatabaseObjectComparator {
    @Override
    public int getPriority(Class<?> objectType, Database database) {
        return MyObject.class.isAssignableFrom(objectType) && 
               database instanceof MyDatabase 
               ? PRIORITY_DATABASE : PRIORITY_NONE;
    }
    
    // REQUIRED: Compare only configuration properties
    // REQUIRED: Exclude state properties from comparison
    // REQUIRED: Unit tests for all comparison scenarios
}
```

## Validation Checkpoints

Before proceeding to implementation, ensure:

```yaml
CHECKPOINT_1_REQUIREMENTS:
  - "Complete requirements document exists"
  - "All properties documented with types and constraints"
  - "Complete SQL syntax examples provided"
  ADDRESSES_CORE_ISSUE: "Complete syntax definition"
  
CHECKPOINT_2_TESTING:
  - "Unit test strategy includes complete SQL string comparison"
  - "Integration test strategy covers ALL generated SQL"
  - "Test harness scope validated and limitations documented"
  ADDRESSES_CORE_ISSUE: "All testing core issues"
  
CHECKPOINT_3_SUCCESS_CRITERIA:
  - "Realistic success criteria established based on framework capabilities"
  - "Scope limitations documented and accepted"
  - "Goalpost changing prevention measures in place"
  ADDRESSES_CORE_ISSUE: "Realistic success criteria"
```

## Next Steps

Continue with one of the following based on your needs:

1. **New to snapshot/diff implementation**: Start with **ai_quickstart.md** for sequential guided implementation
2. **Need specific implementation details**: Go to **part1_object_model.md** to begin detailed implementation
3. **Encountering errors**: Consult **error_patterns_guide.md** for systematic debugging
4. **Need working example**: Review **part5_reference_implementation.md**

## Cross-Reference Links

```yaml
IMPLEMENTATION_SEQUENCE:
  START_HERE: "ai_quickstart.md - Sequential execution guide"
  DETAILED_GUIDES: 
    - "part1_object_model.md"
    - "part2_snapshot_implementation.md" 
    - "part3_diff_implementation.md"
    - "part4_testing_guide.md"
    - "part5_reference_implementation.md"
  DEBUGGING: "error_patterns_guide.md"
  
RELATED_CHANGETYPE_GUIDES:
  PROCESS: "../changetype_implementation/master_process_loop.md"
  REQUIREMENTS: "../changetype_implementation/requirements_creation.md"
  
NAVIGATION: "README.md - Complete folder navigation"
```