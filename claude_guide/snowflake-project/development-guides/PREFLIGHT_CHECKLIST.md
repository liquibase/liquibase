# Pre-Flight Checklist for Snowflake Extension Development

## Environment Verification

### 1. Repository Structure
```bash
# Verify both repositories are present
ls -la liquibase/liquibase-snowflake/
ls -la liquibase-test-harness/

# Verify we're on the correct branch
cd liquibase/liquibase-snowflake
git status
```

### 2. Build Verification
```bash
# Ensure the project builds
cd liquibase/liquibase-snowflake
mvn clean compile

# Check current JAR version
ls -la target/*.jar
```

### 3. Test Harness Setup
```bash
# Verify test harness has Snowflake configuration
cd liquibase-test-harness
cat src/test/resources/liquibase/liquibase.integrationtest.properties | grep snowflake

# Check if extension JAR is in place
ls -la lib/liquibase-snowflake*.jar
```

### 4. Database Connectivity
```bash
# Run init test to verify connection
mvn test -Dtest=ChangeObjectTests -DchangeObjects=init -DdbName=snowflake
```

### 5. Current Implementation Status
```bash
# Check what's already implemented
cd liquibase/liquibase-snowflake
find src/main/java -name "*Schema*.java" -o -name "*Database*.java" -o -name "*Warehouse*.java" | sort

# Check existing tests
find src/test/java -name "*Test.java" | grep -E "(Schema|Database|Warehouse)" | sort
```

## Pre-Implementation Checklist

Before starting ANY implementation:

### Documentation Ready
- [ ] SNOWFLAKE_CHANGETYPE_IMPLEMENTATION_PROMPT.md reviewed
- [ ] SNOWFLAKE_IMPLEMENTATION_PROJECT_PLAN.md current status checked
- [ ] Required guides bookmarked
- [ ] Quick reference available

### Environment Ready
- [ ] Both repositories accessible
- [ ] Maven builds successfully
- [ ] Test harness can connect to Snowflake
- [ ] Latest extension JAR deployed to test harness

### Tools Ready
- [ ] TodoWrite tool tested and working
- [ ] File paths verified
- [ ] Git status clean (no uncommitted changes)

### Knowledge Check
- [ ] Understand difference between NEW_CHANGETYPE_PATTERN_2 and EXISTING_CHANGETYPE_EXTENSION_PATTERN
- [ ] Know the 5-step test harness structure
- [ ] Understand schema vs database vs account level objects
- [ ] Know when to rebuild and redeploy JAR

## Quick Health Check Commands

```bash
# 1. Test a known working change (if any)
mvn test -Dtest=ChangeObjectTests -DchangeObjects=createSchema -DdbName=snowflake

# 2. Verify service registration
jar tf target/liquibase-snowflake-*.jar | grep META-INF/services

# 3. Check XSD is included
jar tf target/liquibase-snowflake-*.jar | grep xsd

# 4. Verify no compilation errors
mvn clean compile

# 5. Run unit tests
mvn test
```

## Ready to Start?

If all checks pass:
1. Review SNOWFLAKE_IMPLEMENTATION_PROJECT_PLAN.md
2. Identify next pending task
3. Create todo items with TodoWrite
4. Begin implementation following the guides

If any checks fail:
1. Fix the issue before proceeding
2. Ask for help if blocked
3. Document the fix for future reference