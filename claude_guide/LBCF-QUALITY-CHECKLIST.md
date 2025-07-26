# LBCF Quality Assurance Checklist and Validation Patterns

## Master Quality Checklist

### Pre-Implementation Quality Gates

#### Documentation Analysis
- [ ] Database vendor documentation URL verified
- [ ] All DDL syntax for target objects documented
- [ ] Attribute types and constraints identified
- [ ] Default values for all attributes noted
- [ ] Database-specific validation rules captured
- [ ] Reserved keywords identified
- [ ] Case sensitivity rules understood

#### Pattern Verification
- [ ] Similar implementation exists in Liquibase
- [ ] Pattern has been successfully used before
- [ ] No experimental approaches planned
- [ ] All uncertainties documented

### Implementation Quality Gates

#### Code Structure
- [ ] Package structure follows Liquibase conventions
- [ ] Class naming consistent with patterns
- [ ] All classes have appropriate JavaDoc
- [ ] No compiler warnings
- [ ] No deprecated API usage

#### Change Class Implementation
- [ ] Extends AbstractChange
- [ ] @DatabaseChange annotation present and complete
- [ ] All attributes have @DatabaseChangeProperty on getters
- [ ] Required fields marked appropriately
- [ ] Validation logic comprehensive
- [ ] getSerializedObjectNamespace() returns correct namespace
- [ ] getConfirmationMessage() provides meaningful feedback
- [ ] supports() method checks correct database type

#### Statement Class Implementation
- [ ] Extends AbstractSqlStatement
- [ ] Immutable (final fields, no setters)
- [ ] Constructor validates required fields
- [ ] All getters present for generator access

#### SQL Generator Implementation
- [ ] Extends AbstractSqlGenerator with correct type parameter
- [ ] getPriority() returns PRIORITY_DATABASE
- [ ] supports() checks database type
- [ ] validate() performs all necessary checks
- [ ] SQL escaping handled correctly
- [ ] Database-specific syntax used
- [ ] No SQL injection vulnerabilities

#### Service Registration
- [ ] liquibase.change.Change registration present
- [ ] liquibase.sqlgenerator.SqlGenerator registration present
- [ ] liquibase.parser.NamespaceDetails registration present
- [ ] All class names in service files are fully qualified
- [ ] No typos in service registration files

#### XSD Schema
- [ ] XSD validates against W3C schema
- [ ] All change attributes documented
- [ ] Required attributes marked with use="required"
- [ ] Default values specified where appropriate
- [ ] Enumerations defined for constrained values
- [ ] Types match Java implementation

### Testing Quality Gates

#### Unit Test Coverage
- [ ] Change class validation tested
- [ ] All attribute combinations tested
- [ ] Statement creation verified
- [ ] SQL generation for all scenarios tested
- [ ] Error conditions tested
- [ ] Null/empty value handling tested
- [ ] >90% line coverage achieved

#### Integration Test Coverage
- [ ] Basic object creation tested
- [ ] All optional attributes tested
- [ ] Object alteration tested (if applicable)
- [ ] Object dropping tested
- [ ] Rollback functionality tested
- [ ] Error scenarios tested
- [ ] Concurrent execution tested
- [ ] Performance benchmarks met

#### Test Quality
- [ ] Tests are independent (no order dependencies)
- [ ] Test data is unique (no conflicts)
- [ ] All tests clean up after execution
- [ ] Test names clearly describe scenario
- [ ] Assertions are specific and meaningful
- [ ] No hard-coded environment-specific values

### Post-Implementation Quality Gates

#### Documentation
- [ ] README.md explains extension purpose
- [ ] Installation instructions provided
- [ ] Usage examples included
- [ ] All public APIs documented
- [ ] Known limitations listed
- [ ] Troubleshooting section included

#### Build and Packaging
- [ ] Maven build succeeds without warnings
- [ ] All tests pass in CI environment
- [ ] JAR manifest includes Liquibase-Package
- [ ] No unnecessary dependencies included
- [ ] License headers present on all source files

## Validation Patterns

### 1. Input Validation Pattern

```java
@Override
public ValidationErrors validate(Database database) {
    ValidationErrors errors = new ValidationErrors();
    
    // Required field validation
    if (isEmpty(requiredField)) {
        errors.addError("requiredField is required");
    }
    
    // Type validation
    if (numericField != null && numericField < 0) {
        errors.addError("numericField must be non-negative");
    }
    
    // Enum validation
    if (enumField != null && !isValidEnumValue(enumField)) {
        errors.addError("enumField must be one of: " + getValidEnumValues());
    }
    
    // Cross-field validation
    if (field1 != null && field2 != null && !areCompatible(field1, field2)) {
        errors.addError("field1 and field2 are incompatible");
    }
    
    // Database compatibility
    if (!supports(database)) {
        errors.addError("Change type not supported on " + database.getShortName());
    }
    
    return errors;
}
```

### 2. SQL Injection Prevention Pattern

```java
@Override
public Sql[] generateSql(Statement statement, Database database, SqlGeneratorChain chain) {
    StringBuilder sql = new StringBuilder();
    
    // Safe: Use database escaping for identifiers
    sql.append("CREATE TABLE ")
       .append(database.escapeObjectName(tableName, Table.class));
    
    // Safe: Use parameterized values where possible
    if (stringValue != null) {
        // Escape single quotes in string literals
        sql.append(" COMMENT '")
           .append(stringValue.replace("'", "''"))
           .append("'");
    }
    
    // Safe: Validate and whitelist enumerated values
    if (enumValue != null && VALID_OPTIONS.contains(enumValue.toUpperCase())) {
        sql.append(" WITH OPTION ").append(enumValue.toUpperCase());
    }
    
    // Never: Don't concatenate user input directly
    // sql.append(" WHERE " + userProvidedWhereClause); // DANGEROUS!
    
    return new Sql[] { new UnparsedSql(sql.toString()) };
}
```

### 3. Database Compatibility Pattern

```java
public class DatabaseCompatibilityValidator {
    
    public ValidationErrors validateCompatibility(Change change, Database database) {
        ValidationErrors errors = new ValidationErrors();
        
        // Version checking
        if (requiresMinimumVersion(change)) {
            String minVersion = getMinimumVersion(change);
            if (!database.isMinimumVersion(minVersion)) {
                errors.addError(String.format(
                    "%s requires %s version %s or later",
                    change.getClass().getSimpleName(),
                    database.getShortName(),
                    minVersion
                ));
            }
        }
        
        // Feature checking
        if (change instanceof SupportsTransient && 
            ((SupportsTransient) change).isTransient() &&
            !database.supportsTransientTables()) {
            errors.addError("Transient tables not supported on " + 
                          database.getShortName());
        }
        
        // Syntax checking
        if (change.requiresIfNotExists() && 
            !database.supportsCreateIfNotExists(change.getObjectType())) {
            errors.addError("IF NOT EXISTS not supported for " + 
                          change.getObjectType() + " on " + 
                          database.getShortName());
        }
        
        return errors;
    }
}
```

### 4. Resource Cleanup Pattern

```java
public class ResourceCleanupPattern {
    
    // In tests
    def "test with cleanup"() {
        given:
        def resourceName = uniqueName()
        def cleanup = new CleanupManager()
        
        try {
            when:
            def resource = createResource(resourceName)
            cleanup.register(() -> dropResource(resourceName))
            
            then:
            resourceExists(resourceName)
            
        } finally {
            cleanup.executeAll()
        }
    }
    
    // Cleanup manager
    class CleanupManager {
        private List<Runnable> cleanupTasks = []
        
        void register(Runnable task) {
            cleanupTasks.add(task)
        }
        
        void executeAll() {
            cleanupTasks.reverse().each { task ->
                try {
                    task.run()
                } catch (Exception e) {
                    log.warn("Cleanup failed", e)
                }
            }
        }
    }
}
```

### 5. Performance Validation Pattern

```java
public class PerformanceValidator {
    
    public void validatePerformance(Change change, Database database) {
        // Check for potentially expensive operations
        if (change instanceof TableScanningChange) {
            long estimatedRows = estimateRowCount(database, change.getTableName());
            if (estimatedRows > 1_000_000) {
                log.warn("Change {} may be slow on large table {} (estimated {} rows)",
                    change.getClass().getSimpleName(),
                    change.getTableName(),
                    estimatedRows
                );
            }
        }
        
        // Check for locking issues
        if (change.requiresExclusiveLock()) {
            log.warn("Change {} requires exclusive lock, may block other operations",
                change.getClass().getSimpleName()
            );
        }
        
        // Suggest optimizations
        if (change instanceof IndexableChange && !change.hasIndex()) {
            log.info("Consider adding an index for better performance");
        }
    }
}
```

## Quality Metrics

### Code Quality Metrics

| Metric | Target | Measurement |
|--------|--------|-------------|
| Unit Test Coverage | >90% | JaCoCo line coverage |
| Integration Test Coverage | >80% | Test scenario coverage |
| Cyclomatic Complexity | <10 | Per method |
| Code Duplication | <5% | CPD analysis |
| Technical Debt | <2 days | SonarQube estimate |

### Runtime Quality Metrics

| Metric | Target | Measurement |
|--------|--------|-------------|
| Change Execution Time | <5 seconds | Per change average |
| Memory Usage | <50MB delta | Heap growth per change |
| SQL Generation Time | <100ms | Average generation time |
| Validation Time | <50ms | Average validation time |

### Test Execution Metrics

| Metric | Target | Measurement |
|--------|--------|-------------|
| Unit Test Execution | <1 minute | Total suite time |
| Integration Test Execution | <5 minutes | Per database |
| Test Reliability | 100% | No flaky tests |
| Test Independence | 100% | Randomized execution |

## Continuous Quality Improvement

### Quality Review Process

1. **Code Review Checklist**
   - [ ] Follows established patterns
   - [ ] No copy-paste errors
   - [ ] Consistent naming
   - [ ] Appropriate error handling
   - [ ] Clear documentation

2. **Test Review Checklist**
   - [ ] Tests are meaningful
   - [ ] Edge cases covered
   - [ ] Cleanup verified
   - [ ] Performance acceptable
   - [ ] No environment dependencies

3. **Integration Review**
   - [ ] Works with existing features
   - [ ] No breaking changes
   - [ ] Performance impact assessed
   - [ ] Documentation updated
   - [ ] Examples provided

### Quality Automation

```groovy
// Automated quality checks in build
task qualityCheck {
    dependsOn 'test'
    dependsOn 'checkstyle'
    dependsOn 'spotbugs'
    dependsOn 'pmd'
    dependsOn 'jacoco'
    
    doLast {
        // Verify coverage
        def coverage = jacoco.reports.xml.destination.text
        def lineCoverage = extractLineCoverage(coverage)
        if (lineCoverage < 0.9) {
            throw new GradleException("Line coverage ${lineCoverage} is below 90%")
        }
        
        // Verify no critical issues
        def spotbugsReport = spotbugs.reports.xml.destination
        if (spotbugsReport.exists() && hasCriticalIssues(spotbugsReport)) {
            throw new GradleException("Critical issues found in SpotBugs report")
        }
    }
}
```

## Common Quality Issues and Solutions

### Issue: Inconsistent NULL Handling

**Problem**: Different behavior for null vs empty values
**Solution**: Standardize null handling
```java
private boolean isEmpty(String value) {
    return value == null || value.trim().isEmpty();
}

// Use consistently
if (isEmpty(field)) {
    errors.addError("field is required");
}
```

### Issue: SQL Injection Vulnerabilities

**Problem**: User input directly in SQL
**Solution**: Always escape or validate
```java
// Bad
sql.append(" WHERE " + userCondition);

// Good
sql.append(" WHERE column = ").append(database.escape(userValue));
```

### Issue: Resource Leaks

**Problem**: Database resources not closed
**Solution**: Use try-with-resources
```java
try (PreparedStatement ps = connection.prepareStatement(sql)) {
    // Use statement
} // Automatically closed
```

### Issue: Flaky Tests

**Problem**: Tests fail intermittently
**Solution**: Ensure test independence
```java
// Bad: Depends on database state
def "test query"() {
    expect:
    queryCount("TEST_TABLE") == 5  // Assumes 5 rows exist
}

// Good: Controls database state
def "test query"() {
    given:
    insertTestData(5)
    
    expect:
    queryCount("TEST_TABLE") == 5
    
    cleanup:
    deleteTestData()
}
```

## Quality Enforcement

### Git Pre-Commit Hook

```bash
#!/bin/bash
# .git/hooks/pre-commit

# Run quality checks
./mvnw clean test checkstyle:check spotbugs:check

if [ $? -ne 0 ]; then
    echo "Quality checks failed. Please fix issues before committing."
    exit 1
fi

# Check for debugging code
if grep -r "System.out.println\|\.printStackTrace()" src/main/java; then
    echo "Debugging code found. Please remove before committing."
    exit 1
fi
```

### CI/CD Quality Gates

```yaml
# GitHub Actions example
quality-gates:
  - name: Test Coverage
    minimum: 90%
    failure: block
    
  - name: Code Smells
    maximum: 10
    failure: warn
    
  - name: Security Hotspots
    maximum: 0
    failure: block
    
  - name: Performance Tests
    threshold: 5s per change
    failure: warn
```

## Summary

Quality is not negotiable in the LBCF framework. By following these checklists and patterns:

1. **Prevent Issues**: Catch problems before they occur
2. **Validate Thoroughly**: Check all inputs and outputs
3. **Test Comprehensively**: Cover all scenarios
4. **Measure Continuously**: Track quality metrics
5. **Improve Iteratively**: Learn from each implementation

Remember: **Quality enables velocity**. Time spent on quality assurance pays dividends in reduced debugging and maintenance.