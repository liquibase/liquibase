# Snowflake Extension Development Guide

This guide provides step-by-step instructions for developing Snowflake features in Liquibase.

## Table of Contents
1. [Development Environment Setup](#development-environment-setup)
2. [Implementation Workflow](#implementation-workflow)
3. [Testing Guide](#testing-guide)
4. [Troubleshooting](#troubleshooting)
5. [Code Examples](#code-examples)

## Development Environment Setup

### Prerequisites
- Java 8 or higher
- Maven 3.6+
- Access to a Snowflake account
- IDE with Maven support (IntelliJ IDEA recommended)

### Configuration

1. **Clone the repository**
   ```bash
   git clone https://github.com/liquibase/liquibase.git
   cd liquibase
   ```

2. **Configure Snowflake connection**
   Create `liquibase-extension-testing/src/main/resources/liquibase.sdk.local.yaml`:
   ```yaml
   liquibase:
     sdk:
       testSystem:
         test: snowflake
         snowflake:
           url: jdbc:snowflake://LWMNXLH-AUB54519.snowflakecomputing.com/
           username: your_username
           password: your_password
   ```

3. **Build the project**
   ```bash
   # Full build (skip maven plugin if it causes issues)
   ./mvnw clean install -DskipTests -pl '!liquibase-maven-plugin'
   
   # Build only Snowflake extension
   ./mvnw clean install -pl liquibase-snowflake -am
   ```

## Implementation Workflow

### Step 1: Define the Change Type

Create the change class in `liquibase-snowflake/src/main/java/liquibase/change/core/`:

```java
package liquibase.change.core;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.CreateWarehouseStatement;

@DatabaseChange(
    name = "createWarehouse", 
    description = "Creates a Snowflake warehouse",
    priority = ChangeMetaData.PRIORITY_DEFAULT
)
public class CreateWarehouseChange extends AbstractChange {
    
    @DatabaseChangeProperty(description = "Name of the warehouse", requiredForDatabase = "snowflake")
    private String warehouseName;
    
    @DatabaseChangeProperty(description = "Size of the warehouse")
    private String warehouseSize = "XSMALL";
    
    // Getters and setters...
    
    @Override
    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[]{
            new CreateWarehouseStatement(
                getWarehouseName(),
                getWarehouseSize()
            )
        };
    }
    
    @Override
    public String getConfirmationMessage() {
        return "Warehouse " + getWarehouseName() + " created";
    }
    
    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors errors = super.validate(database);
        
        if (getWarehouseName() == null) {
            errors.addError("warehouseName is required");
        }
        
        return errors;
    }
}
```

### Step 2: Create the Statement

Create statement in `liquibase-snowflake/src/main/java/liquibase/statement/core/`:

```java
package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

public class CreateWarehouseStatement extends AbstractSqlStatement {
    private final String warehouseName;
    private final String warehouseSize;
    
    public CreateWarehouseStatement(String warehouseName, String warehouseSize) {
        this.warehouseName = warehouseName;
        this.warehouseSize = warehouseSize;
    }
    
    // Getters...
}
```

### Step 3: Implement SQL Generator

Create generator in `liquibase-snowflake/src/main/java/liquibase/sqlgenerator/core/snowflake/`:

```java
package liquibase.sqlgenerator.core.snowflake;

import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.AbstractSqlGenerator;
import liquibase.statement.core.CreateWarehouseStatement;

public class CreateWarehouseGeneratorSnowflake extends AbstractSqlGenerator<CreateWarehouseStatement> {
    
    @Override
    public boolean supports(CreateWarehouseStatement statement, Database database) {
        return database instanceof SnowflakeDatabase;
    }
    
    @Override
    public Sql[] generateSql(CreateWarehouseStatement statement, Database database, SqlGeneratorChain chain) {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE WAREHOUSE IF NOT EXISTS ");
        sql.append(database.escapeObjectName(statement.getWarehouseName(), Warehouse.class));
        
        if (statement.getWarehouseSize() != null) {
            sql.append(" WAREHOUSE_SIZE = ").append(statement.getWarehouseSize());
        }
        
        return new Sql[]{new UnparsedSql(sql.toString())};
    }
}
```

### Step 4: Register Components

Add entries to `liquibase-snowflake/src/main/resources/META-INF/services/`:

1. `liquibase.change.Change`:
   ```
   liquibase.change.core.CreateWarehouseChange
   ```

2. `liquibase.sqlgenerator.SqlGenerator`:
   ```
   liquibase.sqlgenerator.core.snowflake.CreateWarehouseGeneratorSnowflake
   ```

### Step 5: Update XSD Schema

Add to `liquibase-snowflake/src/main/resources/www.liquibase.org/xml/ns/snowflake/liquibase-snowflake-latest.xsd`:

```xml
<xsd:element name="createWarehouse">
    <xsd:complexType>
        <xsd:attribute name="warehouseName" type="xsd:string" use="required"/>
        <xsd:attribute name="warehouseSize" type="warehouseSizeType" default="XSMALL"/>
        <!-- Add more attributes -->
    </xsd:complexType>
</xsd:element>

<xsd:simpleType name="warehouseSizeType">
    <xsd:restriction base="xsd:string">
        <xsd:enumeration value="XSMALL"/>
        <xsd:enumeration value="SMALL"/>
        <xsd:enumeration value="MEDIUM"/>
        <xsd:enumeration value="LARGE"/>
        <xsd:enumeration value="XLARGE"/>
    </xsd:restriction>
</xsd:simpleType>
```

## Testing Guide

### Unit Tests

Create unit test in `liquibase-snowflake/src/test/groovy/liquibase/change/core/`:

```groovy
package liquibase.change.core

import spock.lang.Specification
import liquibase.database.core.SnowflakeDatabase

class CreateWarehouseChangeTest extends Specification {
    
    def "validate requires warehouse name"() {
        given:
        def change = new CreateWarehouseChange()
        def database = new SnowflakeDatabase()
        
        when:
        def errors = change.validate(database)
        
        then:
        errors.hasErrors()
        errors.getErrorMessages().contains("warehouseName is required")
    }
    
    def "generates correct statement"() {
        given:
        def change = new CreateWarehouseChange()
        change.warehouseName = "TEST_WH"
        change.warehouseSize = "SMALL"
        
        when:
        def statements = change.generateStatements(new SnowflakeDatabase())
        
        then:
        statements.length == 1
        statements[0] instanceof CreateWarehouseStatement
        statements[0].warehouseName == "TEST_WH"
        statements[0].warehouseSize == "SMALL"
    }
}
```

### Integration Tests

Create integration test in `liquibase-integration-tests/src/test/groovy/liquibase/test/snowflake/`:

```groovy
package liquibase.test.snowflake

import liquibase.Scope
import liquibase.database.Database
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.test.LiquibaseIntegrationTest
import liquibase.test.TestSystemFactory
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Stepwise

@LiquibaseIntegrationTest
@Stepwise
class WarehouseSnowflakeIntegrationTest extends Specification {
    
    @Shared
    private DatabaseTestSystem testSystem
    
    @Shared
    private Database database
    
    def setupSpec() {
        testSystem = Scope.currentScope.getSingleton(TestSystemFactory).getTestSystem("snowflake")
        if (testSystem?.shouldTest()) {
            database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(
                new JdbcConnection(testSystem.getConnection())
            )
        }
    }
    
    def "create warehouse"() {
        given:
        def warehouseName = "LB_TEST_WH_${System.currentTimeMillis()}"
        
        when:
        def change = new CreateWarehouseChange()
        change.warehouseName = warehouseName
        
        def changeSet = new ChangeSet("test", "test", false, false, null, null, null, null)
        changeSet.addChange(change)
        
        def changelog = new DatabaseChangeLog()
        changelog.addChangeSet(changeSet)
        
        changelog.execute(database, new Contexts())
        
        then:
        warehouseExists(warehouseName)
        
        cleanup:
        dropWarehouse(warehouseName)
    }
}
```

### Test Changelog

Create test changelog in `liquibase-integration-tests/src/test/resources/changelogs/snowflake/warehouse/`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
    xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
    xmlns:snowflake="http://www.liquibase.org/xml/ns/snowflake"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-latest.xsd
        http://www.liquibase.org/xml/ns/snowflake
        http://www.liquibase.org/xml/ns/snowflake/liquibase-snowflake-latest.xsd">

    <changeSet id="1" author="test">
        <snowflake:createWarehouse 
            warehouseName="TEST_WAREHOUSE" 
            warehouseSize="SMALL"
            autoSuspend="300"
            autoResume="true"/>
    </changeSet>
    
    <changeSet id="2" author="test">
        <snowflake:alterWarehouse 
            warehouseName="TEST_WAREHOUSE" 
            warehouseSize="MEDIUM"/>
    </changeSet>
    
    <changeSet id="3" author="test">
        <snowflake:dropWarehouse 
            warehouseName="TEST_WAREHOUSE" 
            ifExists="true"/>
    </changeSet>
</databaseChangeLog>
```

### Running Tests

```bash
# Run all Snowflake tests
./mvnw test -pl liquibase-integration-tests -Dtest="*Snowflake*" -Dliquibase.sdk.testSystem.test=snowflake

# Run specific test class
./mvnw test -pl liquibase-integration-tests -Dtest="WarehouseSnowflakeIntegrationTest" -Dliquibase.sdk.testSystem.test=snowflake

# Run with debug output
./mvnw test -pl liquibase-integration-tests -Dtest="*Snowflake*" -Dliquibase.sdk.testSystem.test=snowflake -Dliquibase.sql.logLevel=FINE
```

## Troubleshooting

### Common Issues

#### 1. XSD Resolution Errors
**Problem**: "Unable to resolve xml entity"
**Solution**: Ensure liquibase-snowflake is on classpath in test module

#### 2. Service Not Found
**Problem**: Change type not recognized
**Solution**: Check META-INF/services registration

#### 3. SQL Generation Errors
**Problem**: Invalid SQL syntax
**Solution**: Test generated SQL directly in Snowflake

#### 4. Test Failures
**Problem**: Integration tests fail
**Solution**: 
- Check Snowflake connection
- Verify test cleanup
- Check for naming conflicts

### Debug Tips

1. **Enable SQL Logging**
   ```bash
   -Dliquibase.sql.logLevel=FINE
   ```

2. **Check Service Registration**
   ```bash
   jar tf liquibase-snowflake/target/liquibase-snowflake-*.jar | grep META-INF/services
   ```

3. **Validate Generated SQL**
   - Add breakpoint in SQL generator
   - Copy generated SQL
   - Test directly in Snowflake

## Code Examples

### Complete Warehouse Implementation

See the following files for a complete example:
- `CreateWarehouseChange.java`
- `AlterWarehouseChange.java`
- `DropWarehouseChange.java`
- `CreateWarehouseStatement.java`
- `CreateWarehouseGeneratorSnowflake.java`

### Advanced Features

#### Multi-Cluster Warehouse
```java
@DatabaseChangeProperty(description = "Minimum number of clusters")
private Integer minClusterCount = 1;

@DatabaseChangeProperty(description = "Maximum number of clusters")
private Integer maxClusterCount = 1;

@DatabaseChangeProperty(description = "Scaling policy")
private String scalingPolicy = "STANDARD";
```

#### Resource Monitor Integration
```java
@DatabaseChangeProperty(description = "Resource monitor name")
private String resourceMonitor;

// In SQL generator
if (statement.getResourceMonitor() != null) {
    sql.append(" RESOURCE_MONITOR = ").append(statement.getResourceMonitor());
}
```

## Best Practices Summary

1. **Always validate input** - Both in change and generator
2. **Use consistent naming** - Follow Liquibase conventions
3. **Test thoroughly** - Unit and integration tests
4. **Document changes** - Update XSD descriptions
5. **Handle rollback** - Implement when possible
6. **Clean up resources** - Especially in tests
7. **Follow patterns** - Look at existing implementations
8. **Check compatibility** - Ensure Snowflake version support

## Next Steps

1. Review existing Snowflake changes for patterns
2. Implement your change type following this guide
3. Write comprehensive tests
4. Submit PR with tests passing
5. Update documentation

For questions or issues, consult the main Liquibase documentation or the development team.