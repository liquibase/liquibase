# CLAUDE.md

This file provides high-level guidance to Claude Code when working on the Liquibase Snowflake Extension.

## 🎯 Current Focus: Snowflake Extension Development

Working on: Snowflake extension development
Validation: Simple INFORMATION_SCHEMA + manual doc review (replaced complex frameworks)
Status: XSD schemas validated as complete for core operations (DATABASE, WAREHOUSE, SEQUENCE)

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

### Connection Credentials (ALWAYS USE WITH TESTS)
```bash
SNOWFLAKE_URL="jdbc:snowflake://rziymts-xbb66763.snowflakecomputing.com/?db=LB_DBEXT_INT_DB&warehouse=LTHDB_TEST_WH&schema=BASE_SCHEMA&role=LB_INT_ROLE" SNOWFLAKE_USER="COMMUNITYKEVIN" SNOWFLAKE_PASSWORD="uQ1lAjwVisliu8CpUTVh0UnxoTUk3"
```

## ⚡ Integration Test Commands

### Unit Tests Only
```bash
mvn test -Dtest="!*IntegrationTest" -q
```

### Integration Tests (Parallel - Default)
```bash
SNOWFLAKE_URL="jdbc:snowflake://rziymts-xbb66763.snowflakecomputing.com/?db=LB_DBEXT_INT_DB&warehouse=LTHDB_TEST_WH&schema=BASE_SCHEMA&role=LB_INT_ROLE" SNOWFLAKE_USER="COMMUNITYKEVIN" SNOWFLAKE_PASSWORD="uQ1lAjwVisliu8CpUTVh0UnxoTUk3" mvn test -Dtest="*GeneratorSnowflakeIntegrationTest" -DforkCount=4 -DreuseForks=true -Dparallel=classes
```

### Integration Tests (Sequential - Debugging)
```bash
SNOWFLAKE_URL="jdbc:snowflake://rziymts-xbb66763.snowflakecomputing.com/?db=LB_DBEXT_INT_DB&warehouse=LTHDB_TEST_WH&schema=BASE_SCHEMA&role=LB_INT_ROLE" SNOWFLAKE_USER="COMMUNITYKEVIN" SNOWFLAKE_PASSWORD="uQ1lAjwVisliu8CpUTVh0UnxoTUk3" mvn test -Dtest="*GeneratorSnowflakeIntegrationTest" -q
```

### Simple Parameter Validation (REQUIRES SNOWFLAKE CONNECTION)
```bash
SNOWFLAKE_URL="jdbc:snowflake://rziymts-xbb66763.snowflakecomputing.com/?db=LB_DBEXT_INT_DB&warehouse=LTHDB_TEST_WH&schema=BASE_SCHEMA&role=LB_INT_ROLE" SNOWFLAKE_USER="COMMUNITYKEVIN" SNOWFLAKE_PASSWORD="uQ1lAjwVisliu8CpUTVh0UnxoTUk3" mvn test -Dtest=SnowflakeParameterValidationTest -q
```

**Simple 3-Step Validation Process:**
1. Query INFORMATION_SCHEMA for actual parameters
2. Compare against XSD schema  
3. Manual Snowflake doc review for gaps
**Time: 15 minutes vs days of complex frameworks**

### All Tests
```bash
SNOWFLAKE_URL="jdbc:snowflake://rziymts-xbb66763.snowflakecomputing.com/?db=LB_DBEXT_INT_DB&warehouse=LTHDB_TEST_WH&schema=BASE_SCHEMA&role=LB_INT_ROLE" SNOWFLAKE_USER="COMMUNITYKEVIN" SNOWFLAKE_PASSWORD="uQ1lAjwVisliu8CpUTVh0UnxoTUk3" mvn test -q
```

### Command Selection
- **Fast execution**: Use parallel format
- **Test failures**: Use sequential for debugging
- **Connection issues**: Verify credentials first

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