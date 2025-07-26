# Claude Guide - Snowflake Extension Documentation

This directory contains comprehensive documentation for the Snowflake extension implementation in Liquibase, designed to preserve knowledge and insights from the development process.

## Contents

### 📋 [SNOWFLAKE_FEATURES_IMPLEMENTED.md](SNOWFLAKE_FEATURES_IMPLEMENTED.md)
Complete reference of all implemented Snowflake features including:
- All change types (Warehouse, Database, Schema operations)
- Properties and configuration options
- XML usage examples
- Testing coverage
- Known limitations

### 🔍 [SNOWFLAKE_IMPLEMENTATION_INSIGHTS.md](SNOWFLAKE_IMPLEMENTATION_INSIGHTS.md)
Key architectural insights and patterns discovered during implementation:
- Three-layer architecture (Change → Statement → SQL Generator)
- Snowflake-specific considerations
- Implementation challenges and solutions
- Testing strategies
- Best practices and pitfalls

### 📖 [SNOWFLAKE_DEVELOPMENT_GUIDE.md](SNOWFLAKE_DEVELOPMENT_GUIDE.md)
Step-by-step guide for developing new Snowflake features:
- Environment setup
- Implementation workflow
- Testing guide
- Troubleshooting tips
- Code examples

### 🧪 [README-TEST-HARNESS.md](README-TEST-HARNESS.md)
Test harness integration documentation:
- Prerequisites for running tests
- Common issues and solutions
- Test harness setup instructions

### 📦 [META-INF-README.md](META-INF-README.md)
Service discovery documentation:
- Explains META-INF/services files
- Service registration patterns
- Java ServiceLoader mechanism

## Quick Links

### Key Directories
- **Change Types**: `liquibase-snowflake/src/main/java/liquibase/change/core/`
- **SQL Generators**: `liquibase-snowflake/src/main/java/liquibase/sqlgenerator/core/snowflake/`
- **Integration Tests**: `liquibase-integration-tests/src/test/groovy/liquibase/test/snowflake/`
- **Test Resources**: `liquibase-integration-tests/src/test/resources/changelogs/snowflake/`

### Important Files
- **Service Registration**: `liquibase-snowflake/src/main/resources/META-INF/services/`
- **XSD Schema**: `liquibase-snowflake/src/main/resources/www.liquibase.org/xml/ns/snowflake/liquibase-snowflake-latest.xsd`
- **Namespace Handler**: `liquibase-snowflake/src/main/java/liquibase/datatype/core/SnowflakeNamespaceDetails.java`

## Development Workflow Summary

1. **Implement Change Type** → extends `AbstractChange`
2. **Create Statement** → implements `SqlStatement`
3. **Build SQL Generator** → extends `AbstractSqlGenerator`
4. **Register Services** → update META-INF/services
5. **Define XSD** → add to liquibase-snowflake-latest.xsd
6. **Write Tests** → unit tests + integration tests
7. **Test with Real Database** → use Snowflake test system

## Maven Commands Reference

```bash
# Build Snowflake extension
./mvnw clean install -pl liquibase-snowflake -am

# Run all Snowflake tests
./mvnw test -pl liquibase-integration-tests -Dtest="*Snowflake*" -Dliquibase.sdk.testSystem.test=snowflake

# Build without problematic modules
./mvnw clean install -DskipTests -pl '!liquibase-maven-plugin'
```

## Key Learnings

1. **Service Discovery** - Always register components in META-INF/services
2. **Case Sensitivity** - Snowflake uppercases unquoted identifiers
3. **Validation** - Implement at both change and generator levels
4. **Testing** - Use real Snowflake database, not mocks
5. **Cleanup** - Always clean up test objects with unique names

## Contributing

When adding new Snowflake features:
1. Follow the established three-layer pattern
2. Review existing implementations for consistency
3. Add comprehensive tests
4. Update documentation
5. Ensure XSD schema is updated

## Future Work

Areas for potential enhancement:
- [ ] File format objects
- [ ] Stage objects
- [ ] Pipe objects
- [ ] Tag support
- [ ] Dynamic tables
- [ ] Masking policies
- [ ] Row access policies
- [ ] External functions

---

*This documentation was created to preserve implementation knowledge and accelerate future Snowflake extension development.*