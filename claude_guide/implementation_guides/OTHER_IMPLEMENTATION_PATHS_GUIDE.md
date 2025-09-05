# Other Implementation Paths Guide
## Focused Guides for Specific Extension Scenarios

### 🎯 Overview

This guide covers implementation paths other than "develop a new database object":

```
Path B: Extend Existing Changetype → Add Snowflake-specific attributes to existing Liquibase changetypes
Path C: Schema Comparison Only → Fix diff/changelog for existing objects  
Path D: SQL Generator Override → Enhance SQL generation for existing changetypes
```

**When to Use This Guide:**
- Adding Snowflake attributes to `createTable`, `createSequence`, etc.
- Fixing broken snapshot/diff functionality
- Overriding SQL generation for Snowflake-specific syntax
- Enhancing existing implementations

---

## 🔧 Path B: Extend Existing Changetype (Namespace Attributes)

### When to Use:
- Adding Snowflake-specific attributes to existing Liquibase changetypes
- Examples: `snowflake:transient="true"` on `createTable`
- Time: 4-6 hours

### Implementation Process:

#### Step 1: XSD Schema Enhancement (30 minutes)

**Add namespace attributes to XSD:**

```xml
<!-- In src/main/resources/www.liquibase.org/xml/ns/snowflake/liquibase-snowflake-latest.xsd -->

<xsd:attribute name="transient" type="xsd:boolean">
    <xsd:annotation>
        <xsd:documentation>Create transient table (no Time Travel)</xsd:documentation>
    </xsd:annotation>
</xsd:attribute>

<xsd:attribute name="clusterBy" type="xsd:string">
    <xsd:annotation>
        <xsd:documentation>Clustering key for table</xsd:documentation>
    </xsd:annotation>
</xsd:attribute>

<xsd:attribute name="dataRetentionTimeInDays" type="xsd:string">
    <xsd:annotation>
        <xsd:documentation>Time Travel retention period (0-90 days)</xsd:documentation>
    </xsd:annotation>
</xsd:attribute>
```

#### Step 2: Parser Integration (45 minutes)

**Update namespace-aware parser:**

```java
// In SnowflakeNamespaceAwareXMLParser.java
public boolean isTargetChangeType(String localName) {
    return "createTable".equals(localName) || 
           "createSequence".equals(localName) ||
           "createSchema".equals(localName) ||
           "yourNewChangeType".equals(localName);
}
```

#### Step 3: Enhanced SQL Generator (2-3 hours)

**Create Snowflake-specific SQL generator:**

```java
public class CreateTableGeneratorSnowflake extends CreateTableGenerator {
    
    @Override
    public int getPriority() {
        return super.getPriority() + 1; // Higher priority than standard generator
    }
    
    @Override
    public boolean supports(CreateTableStatement statement, Database database) {
        return database instanceof SnowflakeDatabase;
    }
    
    @Override
    public Sql[] generateSql(CreateTableStatement statement, Database database, SqlGeneratorChain chain) {
        // Get standard SQL first
        Sql[] standardSql = super.generateSql(statement, database, chain);
        
        // Access namespace attributes
        Map<String, String> attributes = SnowflakeNamespaceAttributeStorage
            .getAttributes(statement.getTableName());
            
        if (attributes == null || attributes.isEmpty()) {
            return standardSql; // No Snowflake-specific attributes
        }
        
        // Enhance SQL with Snowflake-specific syntax
        String enhancedSQL = enhanceWithSnowflakeAttributes(standardSql[0].toSql(), attributes);
        
        return new Sql[]{new UnparsedSql(enhancedSQL, standardSql[0].getAffectedObjects())};
    }
    
    private String enhanceWithSnowflakeAttributes(String baseSql, Map<String, String> attributes) {
        StringBuilder enhanced = new StringBuilder(baseSql);
        
        // Handle transient tables
        if ("true".equalsIgnoreCase(attributes.get("transient"))) {
            enhanced.insert(enhanced.indexOf("TABLE") + 6, "TRANSIENT ");
        }
        
        // Add clustering
        String clusterBy = attributes.get("clusterBy");
        if (clusterBy != null) {
            enhanced.append(" CLUSTER BY (").append(clusterBy).append(")");
        }
        
        // Add data retention
        String dataRetention = attributes.get("dataRetentionTimeInDays");
        if (dataRetention != null) {
            enhanced.append(" DATA_RETENTION_TIME_IN_DAYS = ").append(dataRetention);
        }
        
        return enhanced.toString();
    }
}
```

#### Step 4: Testing (1-2 hours)

**Unit Tests:**

```java
@Test
void testCreateTable_WithTransientAttribute_GeneratesTransientSQL() {
    CreateTableStatement statement = new CreateTableStatement(null, null, "TEST_TABLE");
    
    // Simulate namespace attribute
    Map<String, String> attributes = Map.of("transient", "true");
    SnowflakeNamespaceAttributeStorage.setAttributes("TEST_TABLE", attributes);
    
    Sql[] sql = generator.generateSql(statement, database, null);
    
    String expectedSQL = "CREATE TRANSIENT TABLE TEST_TABLE";
    assertTrue(sql[0].toSql().contains("TRANSIENT"), "Should generate TRANSIENT table SQL");
}
```

**Integration Tests:**

```java
@Test
void testCreateTable_WithSnowflakeAttributes_ExecutesSuccessfully() throws Exception {
    // Test with real Snowflake database
    Database realDatabase = TestDatabaseConfigUtil.getSnowflakeDatabase();
    
    CreateTableStatement statement = createTestTableStatement();
    Map<String, String> attributes = Map.of(
        "transient", "true",
        "dataRetentionTimeInDays", "7"
    );
    SnowflakeNamespaceAttributeStorage.setAttributes("TEST_TABLE", attributes);
    
    Sql[] sql = generator.generateSql(statement, realDatabase, null);
    Executor executor = ExecutorService.getInstance().getExecutor(realDatabase);
    
    // Should execute without errors
    executor.execute(sql[0]);
    
    // Verify table created with correct attributes
    // ... verification logic
}
```

### Success Criteria:
- [ ] XSD schema validates enhanced changelogs
- [ ] Namespace attributes accessible in SQL generator
- [ ] Enhanced SQL generates correctly
- [ ] Real database integration tests pass
- [ ] Backward compatibility maintained

---

## 🔍 Path C: Schema Comparison Only (Fix Snapshot/Diff)

### When to Use:
- Existing changetype works but diff/changelog doesn't
- Objects not appearing in snapshots
- Diff not detecting changes correctly
- Time: 2-4 hours

### Common Issues & Solutions:

#### Issue 1: Objects Not Discovered in Snapshots

**Diagnostic Steps:**
```bash
# Check service registration
grep -r "YourObjectSnapshotGenerator" src/main/resources/META-INF/services/

# Test SQL manually
psql -c "SELECT * FROM INFORMATION_SCHEMA.YOUR_OBJECTS WHERE SCHEMA_NAME = 'TEST';"
```

**Solution Pattern:**
```java
// Fix constructor - wrong parent type
public YourObjectSnapshotGeneratorSnowflake() {
    // WRONG: super(YourObject.class, new Class[]{Table.class});
    // CORRECT for schema-level objects:
    super(YourObject.class, new Class[]{Schema.class});
    // CORRECT for account-level objects:  
    // super(YourObject.class, new Class[]{Account.class});
}

// Fix priority method
@Override
public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
    if (database instanceof SnowflakeDatabase) {
        if (YourObject.class.isAssignableFrom(objectType)) {
            return PRIORITY_DATABASE; // Not PRIORITY_NONE!
        }
    }
    return PRIORITY_NONE;
}
```

#### Issue 2: Diff Not Detecting Changes

**Solution - Fix Comparator:**
```java
public class YourObjectComparator implements DatabaseObjectComparator {
    
    // Exclude state properties from comparison
    private static final String[] EXCLUDED_STATE_FIELDS = {
        "createdTime", "owner", "lastModified", "state"
    };
    
    @Override
    public ObjectDifferences findDifferences(DatabaseObject obj1, DatabaseObject obj2,
                                           Database database, CompareControl control,
                                           DatabaseObjectComparatorChain chain, Set<String> exclude) {
        
        // Add excluded state fields to exclusion set
        exclude = new HashSet<>(exclude);
        exclude.addAll(Arrays.asList(EXCLUDED_STATE_FIELDS));
        
        return chain.findDifferences(obj1, obj2, database, control, exclude);
    }
}
```

#### Issue 3: Account-Level Objects Not Working

**Solution - Unified Extensibility Framework:**
```java
// Use proven pattern from SnowflakeExtensionDiffGeneratorSimple
public class YourObjectSnapshotGeneratorSnowflake extends JdbcSnapshotGenerator {
    
    public YourObjectSnapshotGeneratorSnowflake() {
        super(YourObject.class, new Class[]{Account.class}); // Account-level
    }
    
    @Override
    protected void addTo(DatabaseObject foundObject, DatabaseSnapshot snapshot) {
        if (foundObject instanceof Account) {
            Account account = (Account) foundObject;
            
            // Use SHOW command (no parameters for account-level)
            String sql = "SHOW YOUR_OBJECTS";
            
            // Execute query and process results
            // CRITICAL: Add to both account AND top-level snapshot
            for (/* each result */) {
                YourObject object = new YourObject();
                // ... populate from result set
                
                account.addDatabaseObject(object);
                
                // CRITICAL: Also add to top-level snapshot for diff access
                try {
                    snapshot.include(object);
                } catch (InvalidExampleException e) {
                    // Handle appropriately
                }
            }
        }
    }
}
```

### Quick Fix Testing:
```java
@Test
void testSnapshotGeneration_RealDatabase_DiscoversObjects() throws Exception {
    Database realDatabase = TestDatabaseConfigUtil.getSnowflakeDatabase();
    
    DatabaseSnapshot snapshot = SnapshotGeneratorFactory.getInstance()
        .createSnapshot(defaultSchema, realDatabase, new SnapshotControl(realDatabase));
    
    Set<YourObject> objects = snapshot.get(YourObject.class);
    assertFalse(objects.isEmpty(), "Should discover existing objects");
}
```

---

## ⚙️ Path D: SQL Generator Override (Enhance SQL Generation)

### When to Use:
- Standard SQL doesn't work for Snowflake
- Need Snowflake-specific syntax enhancements
- Examples: Column operations, sequence operations
- Time: 1-3 hours

### Implementation Pattern:

```java
public class CreateSequenceGeneratorSnowflake extends CreateSequenceGenerator {
    
    @Override
    public int getPriority() {
        return super.getPriority() + 1; // Higher priority than standard
    }
    
    @Override
    public boolean supports(CreateSequenceStatement statement, Database database) {
        return database instanceof SnowflakeDatabase;
    }
    
    @Override
    public Sql[] generateSql(CreateSequenceStatement statement, Database database, SqlGeneratorChain chain) {
        StringBuilder sql = new StringBuilder("CREATE SEQUENCE ");
        sql.append(database.escapeSequenceName(statement.getCatalogName(), 
                                               statement.getSchemaName(), 
                                               statement.getSequenceName()));
        
        // Snowflake-specific enhancements
        if (statement.getStartValue() != null) {
            sql.append(" START WITH ").append(statement.getStartValue());
        }
        if (statement.getIncrementBy() != null) {
            sql.append(" INCREMENT BY ").append(statement.getIncrementBy());
        }
        
        // Snowflake-specific: Handle ORDER/NOORDER
        if (statement.getOrdered() != null) {
            sql.append(statement.getOrdered() ? " ORDER" : " NOORDER");
        }
        
        return new Sql[]{new UnparsedSql(sql.toString(), getAffectedSequence(statement))};
    }
}
```

### Testing:
```java
@Test
void testGenerateSQL_SnowflakeSequence_ProducesCorrectSQL() {
    CreateSequenceStatement statement = new CreateSequenceStatement(null, null, "TEST_SEQ");
    statement.setStartValue(BigInteger.valueOf(100));
    statement.setIncrementBy(BigInteger.valueOf(5));
    statement.setOrdered(true);
    
    Sql[] sql = generator.generateSql(statement, database, null);
    
    String expectedSQL = "CREATE SEQUENCE TEST_SEQ START WITH 100 INCREMENT BY 5 ORDER";
    assertEquals(expectedSQL, sql[0].toSql());
}
```

---

## 🎯 Success Criteria Summary

### Path B - Extend Existing Changetype:
- [ ] XSD schema enhancement complete
- [ ] Namespace attributes accessible in SQL generator
- [ ] Enhanced SQL generation works
- [ ] Backward compatibility maintained
- [ ] Integration tests pass

### Path C - Schema Comparison Only:
- [ ] Objects discovered in snapshots
- [ ] Diff detects changes correctly
- [ ] Account-level objects work (if applicable)
- [ ] Service registration fixed
- [ ] Real database tests pass

### Path D - SQL Generator Override:
- [ ] Snowflake-specific SQL generated correctly
- [ ] Higher priority than standard generator
- [ ] All SQL variations covered
- [ ] Integration tests with real database pass

---

## 🚀 Quick Reference Commands

### Diagnostic Commands:
```bash
# Check service registrations
find src/main/resources/META-INF/services -name "*.SnapshotGenerator" -exec grep -H "YourObject" {} \;

# Test SQL manually
snowsql -c myconnection -q "SELECT * FROM INFORMATION_SCHEMA.YOUR_OBJECTS;"

# Check generator priorities
mvn test -Dtest="*GeneratorTest" -q
```

### Testing Commands:
```bash
# Unit tests only
mvn test -Dtest="!*IntegrationTest" -q

# Integration tests only  
mvn test -Dtest="*IntegrationTest" -q

# Specific object tests
mvn test -Dtest="*YourObject*Test*" -q
```

---

*This guide provides focused implementation paths for scenarios other than developing new database objects. Each path includes specific patterns, common issues, and proven solutions based on successful implementations.*