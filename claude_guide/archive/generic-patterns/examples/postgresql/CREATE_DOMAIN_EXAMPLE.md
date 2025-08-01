# PostgreSQL CREATE DOMAIN Example

## Overview

This example demonstrates implementing a PostgreSQL-specific feature (CREATE DOMAIN) in Liquibase. CREATE DOMAIN allows defining custom data types with constraints.

## Files

1. **CreateDomainChange.java**
   - Main change class extending `AbstractChange`
   - Validates inputs and generates statements
   - Database support checking (PostgreSQL only)

2. **CreateDomainStatement.java**
   - Data transfer object holding domain properties
   - Clean separation from Change class

3. **CreateDomainGeneratorPostgreSQL.java**
   - Generates PostgreSQL-specific SQL
   - Handles all domain options (DEFAULT, NOT NULL, CHECK, COLLATE)

4. **CreateDomainTest.java**
   - Unit tests demonstrating test patterns
   - Validation testing
   - SQL generation testing

## Key Patterns Demonstrated

### Database-Specific Support
```java
@Override
public boolean supports(Database database) {
    return database instanceof PostgresDatabase;
}
```

### Proper Validation
```java
@Override
public ValidationErrors validate(Database database) {
    ValidationErrors errors = super.validate(database);
    if (domainName == null || domainName.trim().isEmpty()) {
        errors.addError("domainName is required");
    }
    // ... more validation
}
```

### SQL Generation
```java
sql.append("CREATE DOMAIN ")
   .append(database.escapeObjectName(statement.getDomainName(), Domain.class))
   .append(" AS ")
   .append(statement.getDataType());
```

## What's Missing

This example lacks (intentionally, for clarity):
- Service registration files
- XSD schema definition  
- Test harness tests
- Rollback support
- Update documentation

## Learning Points

1. **Separation of Concerns**: Change → Statement → Generator
2. **Database Specificity**: Feature only for PostgreSQL
3. **Validation Pattern**: Check required fields and database support
4. **Testing Approach**: Unit tests for each component

## Applying to Snowflake

When implementing Snowflake features:
1. Replace `PostgresDatabase` with `SnowflakeDatabase`
2. Adjust SQL syntax for Snowflake
3. Add Snowflake-specific attributes
4. Follow Snowflake naming conventions (e.g., uppercase defaults)