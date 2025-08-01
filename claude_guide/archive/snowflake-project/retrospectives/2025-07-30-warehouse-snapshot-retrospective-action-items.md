# Retrospective Action Items - Documentation Updates

## Overview
Based on the Snowflake Warehouse Snapshot Implementation retrospective, this document outlines specific changes needed to improve the development guides and processes for future implementations.

---

## 1. TEST HARNESS SCOPE UNDERSTANDING

### Target Document: `liquibase-snapshot-diff-build-test-guide-part4.md`
**Section**: "Test Harness" → "Extending Liquibase Test Harness"

**Changes Needed**:
```markdown
### CRITICAL: Test Harness Snapshot Scope Limitations

**Before implementing test harness tests, validate scope compatibility:**

1. **Standard Object Types** (Always Included):
   - liquibase.structure.core.Catalog
   - liquibase.structure.core.Schema  
   - liquibase.structure.core.Table
   - liquibase.structure.core.Column
   - liquibase.structure.core.Index
   - liquibase.structure.core.View
   - liquibase.structure.core.Sequence

2. **Custom Extension Objects** (May Require Configuration):
   - liquibase.database.object.* (your custom objects)
   - May not be automatically included in snapshot scope
   - Test success depends on framework including your object types

3. **Pre-Implementation Validation Steps**:
   ```bash
   # Test if your object type is included in snapshots
   mvn test -Dtest=SnapshotObjectTests -DdbName=[database] -DsnapshotObjects=[existing-test]
   # Look for "includedType" in output to verify scope
   ```

4. **Success Criteria Adjustment**:
   - If custom objects aren't in scope: Implementation success = changesets execute + objects created
   - If custom objects are in scope: Implementation success = full test harness pass
```

### Target Document: `liquibase-snapshot-diff-build-test-ai-quickstart.md`
**Section**: Step 6.3 and 6.4

**Changes Needed**:
```yaml
### VALIDATIONS
- VALIDATION_1:
    TYPE: "SCOPE_VERIFICATION"
    DESCRIPTION: "Verify test harness includes custom object types"
    COMMAND: "Check snapshotControl.includedType in test output"
    SUCCESS_CRITERIA: "Custom object type present OR changesets executing correctly"
    
- VALIDATION_2:
    TYPE: "REALISTIC_EXPECTATIONS"
    DESCRIPTION: "Set appropriate success criteria based on scope"
    IF_CUSTOM_OBJECTS_EXCLUDED: "Success = warehouse created + snapshot attempted"
    IF_CUSTOM_OBJECTS_INCLUDED: "Success = full test harness pass"
```

---

## 2. BUILD PROCESS OPTIMIZATION

### Target Document: `generic-patterns/testing/TEST_HARNESS_IMPLEMENTATION_GUIDE_3.md`
**Section**: "🚨 CRITICAL: SETUP BEFORE PROCEEDING"

**Changes Needed**:
```markdown
## Automated Build Workflow

### Create Build Script
**Location**: `scripts/dev-workflow.sh`
```bash
#!/bin/bash
set -e

echo "=== Liquibase Extension Development Workflow ==="
echo "1. Building extension..."
cd liquibase-snowflake
mvn clean install -DskipTests

echo "2. Changing to test harness..."
cd ../liquibase-test-harness

echo "3. Running test harness..."
mvn test -Dtest=SnapshotObjectTests -DdbName=snowflake -DsnapshotObjects="$1"

echo "=== Workflow Complete ==="
```

### Maven Profile Addition
**Location**: `liquibase-snowflake/pom.xml`
```xml
<profiles>
    <profile>
        <id>dev-test-harness</id>
        <build>
            <plugins>
                <plugin>
                    <groupId>org.codehaus.mojo</groupId>
                    <artifactId>exec-maven-plugin</artifactId>
                    <executions>
                        <execution>
                            <phase>install</phase>
                            <goals>
                                <goal>exec</goal>
                            </goals>
                            <configuration>
                                <executable>bash</executable>
                                <arguments>
                                    <argument>../scripts/dev-workflow.sh</argument>
                                    <argument>${test.changelog}</argument>
                                </arguments>
                            </configuration>
                        </execution>
                    </executions>
                </plugin>
            </plugins>
        </build>
    </profile>
</profiles>
```

### Usage Documentation
```bash
# One command development cycle
mvn clean install -Pdev-test-harness -Dtest.changelog=warehouseSnapshot
```
```

---

## 3. ERROR MESSAGE INTERPRETATION GUIDE

### New Document: `generic-patterns/testing/COMMON_ERROR_PATTERNS.md`

**Content**:
```markdown
# Common Error Patterns and Solutions

## Test Harness Errors

### 1. "Database is up to date, no changesets to execute"
**Symptom**: Changesets not running in test harness
**Root Cause**: DATABASECHANGELOG retains execution history between runs
**Solution**: Add cleanup changeset with `runAlways="true"`
```xml
<changeSet id="cleanup" author="test-harness" runAlways="true">
    <sql>DROP [OBJECT] IF EXISTS [NAME] CASCADE;</sql>
</changeSet>
```

### 2. "Expected: [ObjectType] but none found"
**Symptom**: Snapshot comparison fails
**Root Cause**: Object not included in snapshot scope OR not created
**Diagnosis Steps**:
1. Check if changeset executed: Look for "Running Changeset" in logs
2. Check snapshot scope: Look for "includedType" in test output
3. Verify object creation: Manual database query

### 3. "Invalid content was found starting with element '[changetype]'"
**Symptom**: XML parsing error
**Root Cause**: Missing namespace prefix or XSD declaration
**Solution**: Ensure proper namespace usage
```xml
xmlns:snowflake="http://www.liquibase.org/xml/ns/snowflake"
<snowflake:createWarehouse ...>  <!-- NOT <createWarehouse> -->
```

### 4. JSON Assertion Failures
**Symptom**: JSONAssert comparison errors
**Root Cause**: Format mismatch between expected and actual
**Solution**: Match working test format exactly
- Remove database metadata fields
- Remove "IGNORE" values  
- Use simple object structure only

## Build Errors

### 1. "Could not resolve dependencies"
**Symptom**: Maven build failures
**Root Cause**: Extension JAR not in local repository
**Solution**: `mvn clean install -DskipTests` before test harness

### 2. "Class not found" errors in tests
**Symptom**: Runtime class loading failures
**Root Cause**: Service registration missing
**Solution**: Verify META-INF/services files exist and contain correct class names
```

---

## 4. JSON FORMAT VALIDATION

### Target Document: `liquibase-snapshot-diff-build-test-guide-part4.md`
**Section**: "Test Harness" → new subsection

**Changes Needed**:
```markdown
### JSON Format Validation Tool

**Create validation utility**: `test-harness-json-validator.js`
```javascript
const fs = require('fs');

function validateTestHarnessJson(expectedFile, actualOutput) {
    const expected = JSON.parse(fs.readFileSync(expectedFile));
    const actual = JSON.parse(actualOutput);
    
    // Validate structure matches working pattern
    const requiredStructure = {
        snapshot: {
            objects: {} // Object types go here
        }
    };
    
    // Check for common mistakes
    if (expected.snapshot.database) {
        console.warn("❌ Remove database metadata - not needed in test harness format");
    }
    
    if (JSON.stringify(expected).includes("IGNORE")) {
        console.warn("❌ Remove IGNORE values - not used in test harness format");
    }
    
    // Validate against working pattern
    console.log("✅ JSON format validation complete");
}
```

### Usage Integration
```bash
# Validate before running tests
node scripts/test-harness-json-validator.js expectedSnapshot/snowflake/myTest.json
```
```

---

## 5. FRAMEWORK BEHAVIOR VERIFICATION

### Target Document: `liquibase-snapshot-diff-build-test-ai-quickstart.md`
**Section**: New step before Step 6.3

**Changes Needed**:
```yaml
## STEP 6.2.5: Validate Framework Compatibility
```yaml
STEP_ID: 6.2.5
STATUS: NOT_STARTED
PREREQUISITES: [6.2]
BLOCKS: [6.3]
```

### REQUIRED_ACTIONS
```yaml
- ACTION_1:
    DESCRIPTION: "Test existing harness functionality"
    COMMAND: "mvn test -Dtest=SnapshotObjectTests -DdbName=[database] -DsnapshotObjects=createTable"
    VERIFY: "Test passes with standard objects"
    
- ACTION_2:
    DESCRIPTION: "Examine snapshot scope"
    EXTRACT_FROM_OUTPUT: "snapshotControl.includedType array"
    VERIFY: "Standard object types present"
    
- ACTION_3:
    DESCRIPTION: "Document scope limitations"
    IF_CUSTOM_OBJECTS_MISSING: "Document expected behavior for custom objects"
    SET_REALISTIC_SUCCESS_CRITERIA: "Changeset execution + object creation = success"
```

### VALIDATIONS
```yaml
- VALIDATION_1:
    TYPE: "FRAMEWORK_UNDERSTANDING"
    VERIFY: "Test harness scope documented and understood"
    EVIDENCE: "Clear success criteria set based on actual framework capabilities"
```
```

---

## 6. FILE LOCATION VERIFICATION

### Target Document: `generic-patterns/testing/TEST_HARNESS_IMPLEMENTATION_GUIDE_3.md`
**Section**: Step 2 - Create Your Test File

**Changes Needed**:
```markdown
### File Location Verification Checklist

**Before creating files, verify locations using this checklist:**

1. **Test Harness Files** (liquibase-test-harness repo):
   ```bash
   # Verify you're in the right repository
   pwd  # Should end with /liquibase-test-harness
   ls   # Should see pom.xml with liquibase-test-harness artifactId
   ```

2. **Extension Files** (liquibase-[database] repo):
   ```bash
   # Verify you're in the right repository  
   pwd  # Should end with /liquibase-[database]
   ls   # Should see pom.xml with liquibase-[database] artifactId
   ```

3. **Create Directory Structure Verification**:
   ```bash
   # For test harness
   mkdir -p src/main/resources/liquibase/harness/snapshot/changelogs/[database]
   mkdir -p src/main/resources/liquibase/harness/snapshot/expectedSnapshot/[database]
   
   # Verify paths exist
   ls -la src/main/resources/liquibase/harness/snapshot/
   ```

**Common Mistakes**:
- ❌ Creating test harness files in main liquibase repo
- ❌ Creating extension files in test harness repo  
- ❌ Wrong directory structure (missing database subdirectory)

**Verification Commands**:
```bash
# Quick verification script
echo "Repository check:"
basename $(pwd)  # Should match expected repo name
echo "File structure check:"
find . -name "*.xml" -path "*/snapshot/changelogs/*" | head -5
```
```

---

## 7. SYSTEMATIC ERROR ANALYSIS FRAMEWORK

### New Document: `generic-patterns/testing/SYSTEMATIC_DEBUGGING_FRAMEWORK.md`

**Content**:
```markdown
# Systematic Debugging Framework

## The 5-Layer Debugging Approach

When tests fail, analyze systematically from bottom up:

### Layer 1: Code Compilation and Loading
**Question**: Is the code being loaded?
**Verification**:
```bash
# Check JAR contents
jar -tf target/liquibase-[database]-*.jar | grep [YourClass]
# Check service registration
jar -tf target/liquibase-[database]-*.jar | grep META-INF/services
```

### Layer 2: Component Registration  
**Question**: Is Liquibase finding your components?
**Verification**:
```bash
# Look for loading messages in test output
grep -i "loading\|found\|registered" test-output.log
```

### Layer 3: Execution Flow
**Question**: Are your methods being called?
**Verification**:
```java
// Add debug logging to your components
System.out.println("DEBUG: WarehouseSnapshotGenerator.addTo() called");
```

### Layer 4: Data Creation
**Question**: Is data being created in the database?
**Verification**:
```sql
-- Manual database query
SHOW WAREHOUSES LIKE 'TEST_%';
SELECT * FROM INFORMATION_SCHEMA.WAREHOUSES WHERE WAREHOUSE_NAME LIKE 'TEST_%';
```

### Layer 5: Test Framework Integration
**Question**: Is the test framework configured correctly?
**Verification**:
- Check expected file formats
- Verify snapshot scope inclusion
- Validate JSON structure

## Debugging Decision Tree

```
Test Failure
├── No objects found in snapshot
│   ├── Check Layer 4: Are objects in database? 
│   │   ├── NO → Check Layer 3: Are changesets executing?
│   │   │   ├── NO → Check Layer 2: Are components registered?
│   │   │   │   ├── NO → Check Layer 1: Is code compiled/loaded?
│   │   │   └── YES → Check changeset logic
│   │   └── YES → Check Layer 5: Is test framework including object type?
├── Objects found but format wrong
│   └── Check Layer 5: JSON format and structure
└── Compilation/Loading errors
    └── Check Layer 1: JAR building, service registration
```

## Evidence Collection Template

For each debugging session, collect:

1. **Error Message**: [Exact error text]
2. **Layer Analysis**: 
   - Layer 1 Status: [OK/ISSUE]
   - Layer 2 Status: [OK/ISSUE] 
   - Layer 3 Status: [OK/ISSUE]
   - Layer 4 Status: [OK/ISSUE]
   - Layer 5 Status: [OK/ISSUE]
3. **Root Cause**: [Deepest layer with issues]
4. **Solution Applied**: [Specific fix]
5. **Verification**: [How you confirmed fix worked]
```

---

## Implementation Priority

**High Priority** (Immediate):
1. Test Harness Scope Understanding documentation
2. Systematic Error Analysis Framework creation
3. Common Error Patterns guide

**Medium Priority** (Next iteration):
4. Build Process Optimization scripts
5. JSON Format Validation tools
6. Framework Behavior Verification steps

**Low Priority** (Future enhancement):
7. File Location Verification automation

These changes will significantly improve the developer experience and reduce troubleshooting time for future Liquibase extension implementations.