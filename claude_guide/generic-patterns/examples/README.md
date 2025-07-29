# Liquibase Extension Examples

This directory contains reference implementations of Liquibase extensions for different databases. These examples demonstrate proper patterns and structure for implementing change types.

## Directory Structure

```
examples/
├── postgresql/        # PostgreSQL-specific examples
│   └── CreateDomain   # CREATE DOMAIN implementation
└── snowflake/        # Snowflake examples (future)
```

## PostgreSQL Examples

### CreateDomain
A complete implementation of PostgreSQL's CREATE DOMAIN feature, demonstrating:
- Change class with validation
- Statement class for data transfer
- SQL generator with database-specific logic
- Unit tests showing test patterns

**Note**: These are reference implementations showing proper structure. They are not integrated into the build and lack:
- Service registration (META-INF/services)
- XSD schema definitions
- Test harness tests
- Full integration

## Using These Examples

When implementing a new change type:

1. Review the example structure
2. Copy the pattern (not the code directly)
3. Adapt for your specific database and feature
4. Follow the complete implementation guides in:
   - `/claude_guide/roles/developer/patterns/NEW_CHANGETYPE_PATTERN_2.md`
   - `/claude_guide/roles/qa/patterns/TEST_HARNESS_IMPLEMENTATION_GUIDE_2.md`

## Why Keep Examples Separate

- **Clear distinction**: Examples vs. production code
- **Database-specific**: Each database has unique features
- **Reference only**: Not meant to be built or run
- **Learning tool**: Shows patterns without cluttering guides

## Contributing Examples

When adding new examples:
1. Create database-specific subdirectory if needed
2. Include all four core files (Change, Statement, Generator, Test)
3. Add comments explaining database-specific decisions
4. Update this README with what the example demonstrates