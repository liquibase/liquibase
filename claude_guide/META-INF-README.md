# META-INF Service Discovery Files

This directory contains service discovery files that allow Liquibase to automatically find and register Snowflake-specific components.

## Files

### services/liquibase.parser.NamespaceDetails
Registers the `SnowflakeNamespaceDetails` class which maps the Snowflake XML namespace to its XSD schema file.

**Important**: This file is critical for XSD resolution. Without it, XML changelogs using the Snowflake namespace will fail validation with:
```
Unable to resolve xml entity http://www.liquibase.org/xml/ns/snowflake/liquibase-snowflake-latest.xsd
```

### services/liquibase.change.Change
Registers all Snowflake-specific change types (CreateWarehouse, DropWarehouse, etc.) so they can be used in changelogs.

### services/liquibase.database.Database
Registers the `SnowflakeDatabase` class so Liquibase recognizes Snowflake connections.

### services/liquibase.datatype.LiquibaseDataType
Registers Snowflake-specific data type mappings.

### services/liquibase.sqlgenerator.SqlGenerator
Registers SQL generators that create Snowflake-specific SQL syntax.

### services/liquibase.snapshot.SnapshotGenerator
Registers snapshot generators for capturing Snowflake database structure.

## Troubleshooting

If components are not being discovered:
1. Ensure the service files have the exact interface name
2. Check that implementing classes are listed with their full package names
3. Verify the JAR is on the classpath (especially important for test environments)
4. No extra spaces or special characters in service files