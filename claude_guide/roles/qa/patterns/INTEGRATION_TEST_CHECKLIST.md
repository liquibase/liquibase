# Integration Test Checklist

## Content Standards (v1.0 - Created 2025-01-26)
Only validated checklist items from actual test implementations.

---

## Proven Checklist Items

### Pre-Test Environment Check
- [ ] JAR deployed to test harness lib folder
- [ ] JAR timestamp verified (no caching issues)
- [ ] Database connection verified
- [ ] Test harness clean state confirmed

### Test File Structure
- [ ] changelog.xml with test scenarios
- [ ] expectedSql.sql with exact SQL
- [ ] expectedSnapshot.json if testing snapshots
- [ ] All files in correct database subfolder

### Test Execution
- [ ] Run with real database (not mocks)
- [ ] Verify SQL output matches expected
- [ ] Check for proper object escaping
- [ ] Validate error scenarios

### Common Issues
- ❌ JAR not refreshed → false test failures
- ❌ Wrong SQL syntax → validation errors
- ❌ Missing namespace → class not found

*Based on Snowflake test harness experience*