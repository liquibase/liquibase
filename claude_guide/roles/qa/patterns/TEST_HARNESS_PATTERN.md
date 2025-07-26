# Test Harness Execution Pattern

## Pattern Overview
**Validated Through**: Sequence ORDER implementation
**Confidence Level**: 98%

## Three-File Test Structure

Every test harness test requires three files:

### 1. Changelog File
**Path**: `liquibase-test-harness/src/test/resources/changelogs/snowflake/[testname].xml`
```xml
<changeSet id="1" author="test">
    <snowflake:createSequence sequenceName="test_seq" ordered="true"/>
</changeSet>
```

### 2. Expected SQL File  
**Path**: `liquibase-test-harness/src/test/resources/expectedSql/snowflake/[testname].sql`
```sql
CREATE SEQUENCE test_seq ORDER
```

### 3. Expected Snapshot File
**Path**: `liquibase-test-harness/src/test/resources/expectedSnapshot/snowflake/[testname].json`
```json
{
  "snapshot": {
    "sequences": [{
      "sequence": {
        "name": "test_seq",
        "ordered": true
      }
    }]
  }
}
```

## Execution Command

```bash
mvn test -Dtest=ChangeObjectTests -DchangeObjects=[changeType] -DdbName=snowflake
```

## Validated Best Practices

### ✅ Use Real Database Connections
- Configure in `liquibase.sdk.local.yaml`
- Never use mocks for extension testing
- Catches validation issues mocks would miss

### ✅ Verify Environment First
- Check JAR deployment before debugging code
- Verify test harness is using updated JAR
- Clear Maven cache if seeing stale behavior

### ✅ Systematic Debugging
- One hypothesis at a time
- Check validation chain systematically
- Don't jump between approaches

## Common Issues and Solutions

### JAR Not Updating
**Symptom**: Same errors after code changes
**Solution**: 
1. Verify JAR timestamp: `ls -la liquibase-snowflake/target/*.jar`
2. Clear Maven cache: `rm -rf ~/.m2/repository/org/liquibase/ext/`
3. Rebuild: `./mvnw clean package -pl liquibase-snowflake`

### Validation Errors
**Symptom**: "attribute not allowed" errors
**Solution**:
1. Check XSD is properly generated
2. Verify namespace in changelog matches XSD
3. Check validation priority in change class