# Snowflake Extension - Operational Checklist

## 🚨 MANDATORY PRE-TEST CHECKLIST

**BEFORE any Snowflake testing, ALWAYS complete this checklist:**

### ✅ Build Verification (5 minutes)
```bash
# 1. Clean build
./mvnw clean install -DskipTests -pl '!liquibase-maven-plugin'
# Wait for "BUILD SUCCESS"

# 2. Verify jar exists
ls -la liquibase-snowflake/target/liquibase-snowflake-*.jar
# Should show jar file with recent timestamp

# 3. Copy jar if needed (check your specific setup)
# cp liquibase-snowflake/target/liquibase-snowflake-*.jar [destination]
```

### ✅ Unit Test Validation (3 minutes)
```bash
# Run unit tests first (cheap)
./mvnw test -pl liquibase-snowflake
# ALL unit tests must pass before proceeding
```

### ✅ Integration Test Validation (5 minutes) 
```bash
# Run integration tests (still cheap)
./mvnw test -pl liquibase-integration-tests -Dtest="*Snowflake*" -Dliquibase.sdk.testSystem.test=snowflake
# ALL integration tests must pass before proceeding
```

### ✅ Environment Check (2 minutes)
```bash
# Verify Snowflake connection
# Check liquibase-extension-testing/src/main/resources/liquibase.sdk.local.yaml
# Ensure URL format: LWMNXLH-AUB54519.snowflakecomputing.com
```

### ✅ Test Scope Definition (3 minutes)
- [ ] **What exactly am I testing?** (Write it down)
- [ ] **What's my expected outcome?** (Write it down)  
- [ ] **What's my maximum cost limit?** ($X.XX)
- [ ] **How will I know if it passes/fails?** (Define criteria)

### ✅ Cost Monitoring Setup (1 minute)
```bash
# Log test start time and scope
echo "$(date): Starting Snowflake test - [TEST_DESCRIPTION]" >> snowflake-test-log.txt
```

---

## 🛑 STOP CONDITIONS

**IMMEDIATELY STOP if any of these occur:**

### Build Issues
- Build fails for any reason
- Jar file not generated or old timestamp
- Unit tests fail
- Integration tests fail

### Environment Issues  
- Cannot connect to Snowflake
- Authentication errors
- Database not accessible

### Cost Issues
- Test session exceeds $10 (or set limit)
- Repeated failures burning money
- Unclear what's being tested

### Process Issues
- Unsure what the test should do
- Can't define success criteria
- Making same mistake repeatedly

---

## 🚀 AUTOMATION SCRIPT

Create `build-and-test.sh`:

```bash
#!/bin/bash
set -e  # Exit on any error

echo "🏗️  STEP 1: Building Liquibase Snowflake Extension"
./mvnw clean install -DskipTests -pl '!liquibase-maven-plugin'

echo "🧪 STEP 2: Running Unit Tests"
./mvnw test -pl liquibase-snowflake

echo "🔗 STEP 3: Running Integration Tests"  
./mvnw test -pl liquibase-integration-tests -Dtest="*Snowflake*" -Dliquibase.sdk.testSystem.test=snowflake

echo "✅ READY FOR SNOWFLAKE TESTING"
echo "💰 Remember to set cost limits and define test scope!"
```

Make executable:
```bash
chmod +x build-and-test.sh
```

---

## 📋 TEST HARNESS INVESTIGATION CHECKLIST

**When test harness acts inconsistently:**

### 1. Environment Consistency Check (5 minutes)
- [ ] Same Snowflake account/database each run?
- [ ] Connection parameters unchanged?
- [ ] Test data cleanup between runs?
- [ ] Liquibase version consistent?

### 2. Test Isolation Verification (10 minutes)
- [ ] Run single test in isolation - does it pass?
- [ ] Run same test twice - same result?
- [ ] Check for data left over from previous tests
- [ ] Verify test cleanup procedures

### 3. Failure Pattern Analysis (15 minutes)
- [ ] Which specific tests fail inconsistently?
- [ ] What error messages appear?
- [ ] Any timing-related failures?
- [ ] Network connectivity issues?

### 4. Documentation of Issues (10 minutes)
```markdown
## Test Harness Issue Report
**Date**: [Date]
**Test**: [Specific test name]
**Issue**: [Description of inconsistent behavior]
**Error Message**: [Exact error]
**Frequency**: [How often it fails]
**Pattern**: [When it fails vs succeeds]
**Environment**: [Connection details, versions]
```

---

## 💰 COST CONTROL MEASURES

### Before Each Test Session
```bash
# Set session spending limit
export SNOWFLAKE_TEST_LIMIT=10.00

# Log session start
echo "$(date): Test session start - Limit: $SNOWFLAKE_TEST_LIMIT" >> costs.log
```

### During Testing
- Track time spent per test
- Stop if exceeding budget
- Document what each test validated

### After Testing
```bash
# Log session end and results
echo "$(date): Test session end - Results: [PASS/FAIL] - Lessons: [LEARNED]" >> costs.log
```

---

## 📝 REQUIREMENTS CLARIFICATION TEMPLATE

**Before implementing ANY feature, fill this out:**

```markdown
## Feature Requirement: [FEATURE_NAME]

### What Must Work
- [ ] Specific behavior #1
- [ ] Specific behavior #2  
- [ ] Specific behavior #3

### Success Criteria
- [ ] Test scenario #1 passes
- [ ] Test scenario #2 passes
- [ ] No error conditions

### Acceptance Test
```sql
-- Exact SQL that must work
CREATE WAREHOUSE test_warehouse WITH WAREHOUSE_SIZE = 'XSMALL';
ALTER WAREHOUSE test_warehouse SET WAREHOUSE_SIZE = 'SMALL';
DROP WAREHOUSE test_warehouse;
```

### Definition of Done
- [ ] Code implemented
- [ ] Unit tests pass
- [ ] Integration tests pass
- [ ] Test harness tests pass
- [ ] Documentation updated
```

---

## 🔄 DAILY STANDUP FORMAT

**Start each work session with:**

```markdown
## Daily Standup - [Date]

### Yesterday's Accomplishments
- [What was completed]

### Today's Plan  
- [Specific tasks with time estimates]

### Blockers/Risks
- [Current issues]

### Cost Impact
- Yesterday: $X.XX spent
- Today: $X.XX budget
```

---

## 🎯 RECOVERY SUCCESS PATTERN

### The "Cheap to Expensive" Pattern
1. **Unit Tests** (free) - validate logic
2. **Integration Tests** (cheap) - validate database connection
3. **Simple Snowflake Tests** (moderate cost) - validate basic operations
4. **Complex Snowflake Tests** (expensive) - validate full scenarios

### Never Skip Levels!
- Don't run expensive tests if cheap tests fail
- Fix issues at cheapest level first
- Each level validates before moving up

---

## ⚠️ EMERGENCY RECOVERY PROCEDURE

**If you realize you're stuck in a loop:**

1. **STOP** all testing immediately
2. **DOCUMENT** what you've tried (2 minutes)
3. **ASK** for help (use template)
4. **WAIT** for guidance before continuing

### Help Request Template
```markdown
## EMERGENCY HELP REQUEST

**What I'm trying to accomplish**: [Goal]

**What I've tried** (last 3 attempts):
1. [Attempt 1] - Failed because [reason]
2. [Attempt 2] - Failed because [reason]  
3. [Attempt 3] - Failed because [reason]

**Money spent**: $X.XX

**Time spent**: X hours

**Specific question**: [What do you need to know?]
```

---

## 🎊 QUICK WINS TO CELEBRATE

- ✅ Completed checklist without mistakes
- ✅ All unit tests pass
- ✅ All integration tests pass  
- ✅ Successful Snowflake connection
- ✅ Test harness runs without issues
- ✅ Stayed under budget
- ✅ Clear requirements documented

**Remember**: Every successful run of this checklist is a win that prevents costly mistakes!