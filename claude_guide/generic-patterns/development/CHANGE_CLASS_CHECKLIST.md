# Change Class Implementation Checklist

## Content Standards (v1.0 - Created 2025-01-26)
Only validated checklist items from actual implementations.

---

## Proven Checklist Items

### Change Class Structure
- [ ] Extends AbstractChange
- [ ] @DatabaseChange annotation with name and description
- [ ] getSerializedObjectNamespace() returns "snowflake"
- [ ] All attributes have getters with @DatabaseChangeProperty

### Validation Requirements
- [ ] validate() checks all required fields
- [ ] Error messages are specific and helpful
- [ ] Call super.validate() first

### Service Registration
- [ ] Added to META-INF/services/liquibase.change.Change
- [ ] Fully qualified class name used
- [ ] Verified in built JAR file

### Common Mistakes to Avoid
- ❌ Using mutable statement pattern
- ❌ Forgetting service registration
- ❌ Missing namespace implementation
- ❌ Incomplete validation logic

*Based on actual Snowflake sequence implementation experience*