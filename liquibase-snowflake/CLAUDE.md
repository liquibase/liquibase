# CLAUDE.md

This file provides high-level guidance to Claude Code when working on the Liquibase Snowflake Extension.

## 🎯 MILESTONE ACHIEVED: 80%+ Coverage + Complete Schema Operations ✅

**STATUS**: Major milestone completed (August 2025)
- ✅ **80%+ Test Coverage**: Achieved comprehensive snapshot generator coverage
- ✅ **CreateSchema Operations**: Complete XML parsing, SQL generation, XSD validation  
- ✅ **FileFormat Operations**: Complete CREATE/ALTER/DROP lifecycle
- ✅ **Catalog Naming**: Proper catalogName vs databaseName abstraction implemented
- ✅ **Test Infrastructure**: Clean test patterns with schema isolation

Working on: Advanced Snowflake extension development
Validation: Simple INFORMATION_SCHEMA + manual doc review (replaced complex frameworks)

## ⚡ PRE-FLIGHT VALIDATION (MANDATORY BEFORE ANY SNAPSHOT/DIFF IMPLEMENTATION)

**ALWAYS run these validation checks before starting any new object implementation:**

### ✅ Autonomous Operation Prerequisites
```bash
# Verify TDD enforcement system is available
test -f .scripts/tdd_workflow.sh && echo "✅ TDD workflow available" || echo "❌ TDD workflow missing"
test -f .scripts/validation_functions.sh && echo "✅ Validation functions available" || echo "❌ Validation functions missing"
test -f .templates/object_model_template.java && echo "✅ Templates available" || echo "❌ Templates missing"

# Verify autonomous test commands work
mvn --version >/dev/null 2>&1 && echo "✅ Maven available" || echo "❌ Maven not found"
mvn compile -q >/dev/null 2>&1 && echo "✅ Project compiles" || echo "❌ Compilation issues"

# Count existing autonomous test patterns in CLAUDE.md
grep -c "mvn test.*-Dtest=" CLAUDE.md && echo "autonomous test commands found" || echo "❌ No autonomous test commands"
```

### ✅ Enhanced TDD Enforcement System Status

**VALIDATED ENFORCEMENT MECHANISM**: External behavioral enforcement system with hard validation blocks.

#### Enhanced Validation Checkpoints
- **Template Generation**: Immediate package structure, import, and compilation validation
- **Framework Integration**: Method signature, inheritance, and pattern compliance checks  
- **Micro-Cycle Integration**: Framework compliance validation after each TDD cycle
- **Property Patterns**: Getter/setter consistency and return type validation

#### Enhanced Validation Commands
```bash
# Enhanced template validation (blocks progression on failure)
validate_complete_template_generation ObjectType

# Framework integration compliance (catches inheritance/signature issues)
validate_framework_integration_compliance ObjectType

# Enhanced micro-cycle validation (includes framework integration checks)
validate_micro_cycle_integration TestClass TestMethod ObjectType
```

**Enforcement Result**: Template issues like package structure, method signatures, and framework integration problems are caught immediately rather than discovered during compilation.

### ✅ TDD Enforcement System Status
```bash
# Check if enforcement system is operational
.scripts/tdd_workflow.sh status 2>/dev/null && echo "✅ TDD enforcement operational" || echo "❌ TDD enforcement not initialized"

# Verify checkpoint/validation system
test -d .process_state && test -d .checkpoints && echo "✅ State management ready" || echo "❌ State management not ready"
```

### ✅ Database Connection Validation (For Integration Tests)
```bash
# Verify Snowflake connection using YAML configuration
mvn test -Dtest=SnowflakeParameterValidationTest -q >/dev/null 2>&1 && echo "✅ Database connection works" || echo "❌ Database connection failed"
```

**🚨 CRITICAL: If any pre-flight check fails, STOP and fix the issue before proceeding.**

## 🔄 CRITICAL: Follow the Master Process Loop

**For EVERY task**: Follow `claude_guide/snowflake-project/quick-reference/MASTER_PROCESS_LOOP.md`
- This ensures project tracking and retrospectives happen
- Operational tasks are part of the work, not overhead

## 📚 Essential Guides (In Order of Use)

1. **Process & Workflow**
   - `MASTER_PROCESS_LOOP.md` - The complete process with all file references

2. **Implementation**
   - `NEW_CHANGETYPE_PATTERN_2.md` - For new change types
   - `EXISTING_CHANGETYPE_EXTENSION_PATTERN.md` - For extending existing types
   - `SQL_GENERATOR_OVERRIDE_STEP_BY_STEP.md` - For SQL syntax overrides (column operations)

3. **Testing**
   - `TEST_HARNESS_IMPLEMENTATION_GUIDE_3.md` - After unit tests pass (simplified with schema isolation)

4. **Requirements & Tracking**
   - `detailed_requirements/<changeType>_requirements.md` - Per change type
   - `SNOWFLAKE_IMPLEMENTATION_PROJECT_PLAN.md` - Update continuously

## 🚨 MANDATORY: XSD SCHEMA VALIDATION

**BEFORE implementing ANY new attribute/feature:**

### ✅ Implementation Checklist (Execute in Order)
```
1. ✅ Add attribute to Java Change class  
2. ✅ Add attribute to XSD schema element
3. ✅ Rebuild extension JAR
4. ✅ Install JAR to test harness  
5. ✅ Run test harness validation
6. ✅ Fix any XSD validation errors
```

### 🔍 XSD Locations (Copy-Paste Paths)
```bash
# XSD Schema File
/Users/kevinchappell/Documents/GitHub/liquibase/liquibase-snowflake/src/main/resources/www.liquibase.org/xml/ns/snowflake/liquibase-snowflake-latest.xsd

# Search for element (replace ELEMENT_NAME)
grep -n "ELEMENT_NAME" /Users/kevinchappell/Documents/GitHub/liquibase/liquibase-snowflake/src/main/resources/www.liquibase.org/xml/ns/snowflake/liquibase-snowflake-latest.xsd
```

### ⚠️ Common XSD Validation Errors → Solutions
| Error Pattern | Solution |
|---|---|
| `Attribute 'ATTR_NAME' is not allowed to appear in element 'snowflake:ELEMENT'` | Add `<xsd:attribute name="ATTR_NAME" type="xsd:boolean"/>` to element |
| `cvc-complex-type.3.2.2` | Missing attribute definition in XSD |
| Test harness fails with parsing error | Check XSD schema completeness |

## 🚨 When Things Go Wrong

**DO NOT assume where the bug is!**
- Use systematic debugging (test each layer separately)
- Question "known bugs" - they might be false  
- See troubleshooting in implementation guides

## 🏗️ Repository Structure & File Placement

### CRITICAL: Repository Paths (AI Reference)
```bash
# Extension Repository (Current Working Directory)
/Users/kevinchappell/Documents/GitHub/liquibase/liquibase-snowflake/

# Test Harness Repository (Separate Location)  
/Users/kevinchappell/Documents/GitHub/liquibase-test-harness/
```

### File Placement Rules (NEVER MIX THESE)

**Extension Repository (`liquibase-snowflake/`):**
- ✅ Java implementation files (Change, Statement, SQLGenerator classes)
- ✅ Unit tests (*Test.java, *IntegrationTest.java)
- ✅ XSD schema files (liquibase-snowflake.xsd)
- ✅ Extension documentation (CLAUDE.md, README.md)

**Test Harness Repository (`liquibase-test-harness/`):**
- ✅ Changelog files (`changelogs/snowflake/*.xml`)
- ✅ Expected SQL files (`expectedSql/snowflake/*.sql`)  
- ✅ Expected snapshot files (`expectedSnapshot/snowflake/*.json`)
- ✅ Test harness configuration files

### Project Structure
```
liquibase/
├── liquibase-snowflake/          # Extension module (YOU ARE HERE)
│   ├── src/main/java/           # Implementation
│   ├── src/test/java/           # Unit tests
│   └── src/main/resources/      # XSD, services
└── claude_guide/                 # All guides and patterns
    ├── generic-patterns/         # Reusable patterns
    └── snowflake-project/        # Project-specific
```

## ⚡ Snowflake Database Connection

### YAML Configuration (Used by All Tests)
All tests now use the YAML configuration file instead of environment variables:
```yaml
# Location: src/test/resources/liquibase.sdk.local.yaml
liquibase:
  sdk:
    testSystem:
      snowflake:
        url: "jdbc:snowflake://rziymts-xbb66763.snowflakecomputing.com/?db=LB_DBEXT_INT_DB&warehouse=LTHDB_TEST_WH&schema=BASE_SCHEMA&role=LB_INT_ROLE"
        username: "COMMUNITYKEVIN"
        password: "uQ1lAjwVisliu8CpUTVh0UnxoTUk3"
        catalog: "LB_DBEXT_INT_DB"  
        schema: "BASE_SCHEMA"
        altSchema: "ALT_SCHEMA"
```

## ⚡ Integration Test Commands (80%+ Coverage Achievement Pattern)

### Unit Tests Only (Fast - Now Parallel)
```bash
mvn test -Dtest="!*IntegrationTest" -q
```

### MILESTONE ACHIEVED: Comprehensive Test Commands
```bash
# Snapshot Generators (80%+ Coverage Achieved)
mvn test -Dtest="*SnapshotGenerator*Test*" -q

# FileFormat Complete Testing (MILESTONE ACHIEVED)
mvn test -Dtest="*FileFormat*Test*" -q
mvn test -Dtest="FileFormatSnapshotGeneratorTest" -q  
mvn test -Dtest="FileFormatComparatorTest" -q

# Warehouse Complete Testing (MILESTONE ACHIEVED)  
mvn test -Dtest="*Warehouse*Test*" -q
mvn test -Dtest="WarehouseSnapshotGeneratorTest" -q

# Schema Operations Testing (MILESTONE ACHIEVED)
mvn test -Dtest="*Schema*Test*" -q
mvn test -Dtest="CreateSchemaGeneratorSnowflakeNamespaceTest" -q

# UniqueConstraint Testing (Bug Fixed)
mvn test -Dtest="UniqueConstraintSnapshotGeneratorSnowflakeTest" -q

# Generic object testing patterns (Proven Effective)
mvn test -Dtest="*{OBJECT_TYPE}*Test*" -q
mvn test -Dtest="{OBJECT_TYPE}Test" -q
mvn test -Dtest="*{OBJECT_TYPE}*SnapshotGenerator*Test*" -q
mvn test -Dtest="*{OBJECT_TYPE}*Comparator*Test*" -q
```

### Integration Tests (Parallel - Now Default!)
```bash
# All integration tests now run in parallel by default (4 threads) due to schema isolation
# Uses YAML configuration from src/test/resources/liquibase.sdk.local.yaml
mvn test -Dtest="*IntegrationTest" -q
```

### Integration Tests (Sequential - For Debugging Only)
```bash
# Force sequential execution when debugging specific issues
mvn test -Dtest="*IntegrationTest" -DforkCount=1 -Dparallel=none -q
```

### Simple Parameter Validation (Uses YAML Config)
```bash
# Uses YAML configuration from src/test/resources/liquibase.sdk.local.yaml
mvn test -Dtest=SnowflakeParameterValidationTest -q
```

**Simple 3-Step Validation Process:**
1. Query INFORMATION_SCHEMA for actual parameters
2. Compare against XSD schema  
3. Manual Snowflake doc review for gaps
**Time: 15 minutes vs days of complex frameworks**

### All Tests (Now Fully Parallel!)
```bash
# Runs all unit tests and integration tests in parallel (4 threads each)
# Uses YAML configuration from src/test/resources/liquibase.sdk.local.yaml
mvn test -q
```

### Command Selection
- **Default execution**: Now automatically parallel (4 threads) for fast execution
- **Test failures**: Use sequential format (`-DforkCount=1 -Dparallel=none`) for easier debugging
- **Connection issues**: Verify credentials first
- **Schema isolation**: Enables safe parallel execution for all Snowflake integration tests

## ⚡ JAR BUILD/INSTALL WORKFLOW (MANDATORY AFTER CODE CHANGES)

### 🔧 Build Extension JAR (Copy-Paste Commands)
```bash
# STEP 1: Build JAR
cd /Users/kevinchappell/Documents/GitHub/liquibase/liquibase-snowflake/
mvn clean package -DskipTests -q

# STEP 2: Install to Test Harness  
cd /Users/kevinchappell/Documents/GitHub/liquibase-test-harness/
mvn install:install-file -Dfile=/Users/kevinchappell/Documents/GitHub/liquibase/liquibase-snowflake/target/liquibase-snowflake-0-SNAPSHOT.jar -DgroupId=org.liquibase -DartifactId=liquibase-snowflake -Dversion=0-SNAPSHOT -Dpackaging=jar -q

# STEP 3: Validate Installation
mvn test -Dtest=ChangeObjectTests -DchangeObjects=createDatabase -DdbName=snowflake -q
```

### 🔄 When to Rebuild JAR
```
✅ After ANY Java code changes
✅ After XSD schema updates  
✅ After adding new attributes
✅ Before test harness validation
❌ NOT needed for test harness file changes only
```

### 🚨 Cache Issues → Force Refresh
```bash
# IF installation seems cached/stale
cd /Users/kevinchappell/Documents/GitHub/liquibase-test-harness/
mvn dependency:purge-local-repository -DgroupId=org.liquibase -DartifactId=liquibase-snowflake -Dversion=0-SNAPSHOT --quiet
# THEN re-run install command above
```

## 🚨 CRITICAL: Test Harness File Location Rules

### 🔍 REPOSITORY VERIFICATION (MANDATORY BEFORE ANY FILE OPERATIONS)

#### ✅ Current Directory Check (Copy-Paste Command)
```bash
pwd
```

#### ✅ Required Output Patterns
```bash
# Extension Work - MUST show:
/Users/kevinchappell/Documents/GitHub/liquibase/liquibase-snowflake

# Test Harness Work - MUST show:  
/Users/kevinchappell/Documents/GitHub/liquibase-test-harness
```

#### ✅ Navigate Between Repos (Copy-Paste Commands)
```bash
# Go to Extension Repo
cd /Users/kevinchappell/Documents/GitHub/liquibase/liquibase-snowflake/

# Go to Test Harness Repo  
cd /Users/kevinchappell/Documents/GitHub/liquibase-test-harness/
```

### Before Creating ANY Test Harness Files:
1. **ALWAYS run**: `pwd` and verify output
2. **IF in extension repo**: Use navigation command above FIRST
3. **Test harness files ONLY go in**: `/Users/kevinchappell/Documents/GitHub/liquibase-test-harness/`

### Navigation Commands (Copy-Paste Ready):
```bash
# From extension to test harness
cd /Users/kevinchappell/Documents/GitHub/liquibase-test-harness/

# From test harness to extension  
cd /Users/kevinchappell/Documents/GitHub/liquibase/liquibase-snowflake/
```

### File Type Identification:
- **Contains `<changeSet>` or `<databaseChangeLog>`** → Test harness repo
- **Contains Java classes or unit tests** → Extension repo
- **File extensions**: `.xml` changelog files → Test harness repo
- **File extensions**: `.java` implementation files → Extension repo

### Recovery from Misplaced Files:
```bash
# Move test harness files from extension to test harness repo
mv /Users/kevinchappell/Documents/GitHub/liquibase/liquibase-snowflake/changelogs/* /Users/kevinchappell/Documents/GitHub/liquibase-test-harness/src/main/resources/liquibase/harness/change/changelogs/snowflake/
```

## 🔗 Quick Links

- Build Commands: See `SNOWFLAKE_QUICK_REFERENCE.md`
- Architecture Details: See `LIQUIBASE_ARCHITECTURE_OVERVIEW.md`
- Test Structure: `src/test/java/README_TEST_STRUCTURE.md`
- Current Status: Check project plan

---
**Remember**: This is a high-level entry point. For specifics, follow the guides.