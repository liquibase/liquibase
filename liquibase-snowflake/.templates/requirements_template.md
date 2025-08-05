# ${ObjectType} Snapshot/Diff Requirements

## Research Sources

### Official Snowflake Documentation
- **CREATE ${OBJECT_TYPE_UPPER}**: ${CreateDocumentationURL}
- **SHOW ${OBJECT_TYPE_UPPER}S**: ${ShowDocumentationURL}  
- **DESCRIBE ${OBJECT_TYPE_UPPER}**: ${DescribeDocumentationURL}

### Database Introspection Queries
```sql
-- Query 1: List all ${ObjectType}s
${ShowObjectsQuery}

-- Query 2: Get detailed ${ObjectType} properties  
${DescribeObjectQuery}

-- Query 3: Additional properties from INFORMATION_SCHEMA
${InformationSchemaQuery}
```

## Property Analysis

| Property | Type | Required | Default | Comparison | Notes |
|----------|------|----------|---------|------------|--------|
${PropertyAnalysisTable}

## Implementation Requirements

### Object Model (${ObjectType}.java)
- [ ] Extends AbstractDatabaseObject
- [ ] Implements getName(), setName(), getSchema(), setSchema()
- [ ] Implements equals() and hashCode() based on identity properties
- [ ] Property getters/setters for all ${PropertyCount}+ properties
- [ ] Constructor with name parameter
- [ ] Proper toString() implementation

### Snapshot Generator (${ObjectType}SnapshotGenerator.java)
- [ ] Extends JdbcSnapshotGenerator
- [ ] Supports ${ObjectType}.class with Schema parent
- [ ] Implements snapshotObject() with database queries
- [ ] Implements addTo() for snapshot building
- [ ] Proper priority handling (PRIORITY_DEFAULT for supported, PRIORITY_NONE for others)
- [ ] Error handling for database exceptions

### Diff Comparator (${ObjectType}Comparator.java)
- [ ] Extends DefaultDatabaseObjectComparator
- [ ] Implements isSameObject() based on identity properties
- [ ] Implements compareObjects() with property-by-property comparison
- [ ] Proper priority handling (PRIORITY_TYPE for supported, PRIORITY_NONE for others)
- [ ] Returns empty ObjectDifferences for unsupported types

### Test Coverage Requirements
Each component requires comprehensive test coverage:

#### Positive Tests (Expected Success Cases)
- [ ] Basic functionality works correctly
- [ ] Valid inputs produce expected outputs
- [ ] Integration with framework components

#### Negative Tests (Expected Failure Cases)  
- [ ] Invalid inputs handled gracefully
- [ ] Null values don't cause exceptions
- [ ] Unsupported operations return appropriate responses

#### Boundary Tests (Edge Conditions)
- [ ] Empty/minimum values handled correctly
- [ ] Maximum length/size limits respected
- [ ] Edge cases in property values

#### Edge Case Tests (Complex Scenarios)
- [ ] Equals/hashCode contracts maintained
- [ ] Exception handling and recovery
- [ ] Complex property combinations

### Service Registration
- [ ] ${ObjectType}SnapshotGenerator registered in META-INF/services/liquibase.snapshot.SnapshotGenerator
- [ ] ${ObjectType}Comparator registered in META-INF/services/liquibase.diff.compare.DatabaseObjectComparator

## Property Details

${DetailedPropertyDocumentation}

## Quality Gates

### Phase Completion Criteria
- [ ] All unit tests pass (8+ tests per component)
- [ ] Code compiles without warnings
- [ ] Services properly registered
- [ ] Integration tests pass with real Snowflake database
- [ ] XSD schema updated if needed for changetype integration

### Validation Commands
```bash
# Unit tests
mvn test -Dtest="*${ObjectType}*Test*" -q

# Compilation check
mvn compile -q

# Integration test (requires Snowflake connection)
SNOWFLAKE_URL="jdbc:snowflake://..." mvn test -Dtest="*${ObjectType}*IntegrationTest" -q
```

## Implementation Notes

${ImplementationNotes}