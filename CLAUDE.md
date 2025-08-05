# CLAUDE.md

This file provides high-level guidance to Claude Code when working on the Liquibase Snowflake Extension.

## 🎯 Current Focus: Enhancing the Liquibase-Snowflake Extension

The focus is on enhancing the liquibase-snowflake extension with comprehensive support for Snowflake's unique features including warehouses, file formats, advanced data types, and database operations.

## 🏗️ Extension Architecture

The liquibase-snowflake extension provides comprehensive support for Snowflake's unique features:

### Core Components
- **Changes**: Database operations (CREATE/ALTER/DROP) for Database, FileFormat, Schema, Warehouse, Table, Sequence
- **Database Objects**: Snowflake-specific objects (Database, FileFormat, Schema, Warehouse)
- **Data Types**: Advanced Snowflake types (Array, Binary, Geography, Geometry, Variant, Object, etc.)
- **Snapshot/Diff**: Database introspection and schema comparison capabilities
- **SQL Generators**: Snowflake-specific SQL generation for all operations

### Key Snowflake Features Supported
- **Warehouses**: Virtual compute clusters with size, auto-suspend, and scaling properties
- **File Formats**: CSV, JSON, Parquet, XML data loading configurations
- **Advanced Data Types**: Semi-structured data (VARIANT, OBJECT, ARRAY), geospatial types
- **Database/Schema Management**: Multi-level namespace support
- **Sequences**: Snowflake-specific sequence features and properties

## 🧪 Testing Commands

```bash
# Extension-wide tests
mvn test -q
mvn compile -q

# Specific component tests
mvn test -Dtest="*FileFormat*Test*" -q
mvn test -Dtest="*Warehouse*Test*" -q
mvn test -Dtest="*Database*Test*" -q
mvn test -Dtest="*Schema*Test*" -q
mvn test -Dtest="*Sequence*Test*" -q

# Integration tests
mvn test -Dtest="*IntegrationTest" -q
```


## 📁 Extension Structure

```
liquibase-snowflake/
├── src/main/java/liquibase/
│   ├── change/core/              # Change implementations
│   │   ├── *DatabaseChange.java  # Database operations
│   │   ├── *FileFormatChange.java # File format operations  
│   │   ├── *SchemaChange.java     # Schema operations
│   │   └── *WarehouseChange.java  # Warehouse operations
│   ├── database/                 # Database connection & objects
│   │   ├── core/SnowflakeDatabase.java
│   │   └── object/               # Database object models
│   ├── datatype/core/            # Snowflake-specific data types
│   ├── diff/output/              # Schema comparison logic
│   ├── snapshot/jvm/             # Database introspection
│   ├── sqlgenerator/             # SQL generation
│   └── statement/core/           # SQL statement models
├── src/main/resources/
│   ├── META-INF/services/        # Service registrations
│   └── www.liquibase.org/xml/ns/snowflake/ # XSD schemas
└── src/test/                     # Comprehensive test suite
```

## 🔧 Development Patterns

### Adding New Snowflake Features
1. **Database Object** → Create object model in `database/object/`
2. **Change Implementation** → Add change class in `change/core/`
3. **SQL Generation** → Implement generator in `sqlgenerator/`
4. **Snapshot Support** → Add snapshot generator in `snapshot/jvm/`
5. **Schema Comparison** → Add comparator in `diff/output/`
6. **Service Registration** → Register in `META-INF/services/`
7. **XSD Schema** → Update schema in `www.liquibase.org/xml/ns/snowflake/`
8. **Testing** → Add comprehensive unit and integration tests

### Extension Guidelines
- **Follow Existing Patterns**: Mirror the structure of existing implementations
- **Snowflake-Specific**: Leverage Snowflake's unique capabilities (warehouses, file formats, etc.)
- **Comprehensive Testing**: Include both unit tests and integration tests with real Snowflake connections
- **Service Registration**: Ensure all components are properly registered for discovery

## 🔍 Common Extension Areas

### Current Implementation Status
- ✅ **Warehouses**: Full CREATE/ALTER/DROP support with advanced properties
- ✅ **File Formats**: CSV, JSON, Parquet support with comprehensive options
- ✅ **Databases**: Multi-database management with Snowflake-specific features
- ✅ **Schemas**: Enhanced schema operations with namespace support
- ✅ **Tables**: Advanced table features with Snowflake-specific properties
- ✅ **Sequences**: Snowflake sequence implementation with unique features
- ✅ **Data Types**: Comprehensive support for Snowflake's advanced types

### Enhancement Opportunities
- **Stages**: External and internal stage management
- **Pipes**: Data loading pipeline automation
- **Tasks**: Scheduled task execution
- **Streams**: Change data capture streams
- **Views**: Materialized and secure views
- **Functions**: User-defined functions (UDF)
- **Procedures**: Stored procedure support

## 🚀 Getting Started

### Quick Development Setup
1. **Navigate to extension**: `cd liquibase-snowflake/`
2. **Compile**: `mvn compile`
3. **Run tests**: `mvn test`
4. **Check specific component**: `mvn test -Dtest="*ComponentName*Test*"`

### Key Files to Know
- **Service Registration**: `src/main/resources/META-INF/services/` - Where components are registered
- **XSD Schema**: `src/main/resources/www.liquibase.org/xml/ns/snowflake/` - XML schema definitions
- **Integration Tests**: Look for `*IntegrationTest.java` files - these test against real Snowflake instances

## 📖 Implementation Guides

### Core Implementation Workflows
- **Changetype Implementation**: `claude_guide/implementation_guides/CHANGETYPE_IMPLEMENTATION_GUIDE.md` - Complete workflow for adding new database operations (CREATE/ALTER/DROP) with Task tool integration for requirements research
- **Snapshot/Diff Implementation**: `claude_guide/implementation_guides/SNAPSHOT_DIFF_IMPLEMENTATION_GUIDE.md` - Database introspection and schema comparison with Task tool integration for comprehensive property analysis
- **Diff/Generate Changelog**: `claude_guide/implementation_guides/DIFF_GENERATE_CHANGELOG_IMPLEMENTATION_GUIDE.md` - Schema difference detection and changelog generation with Task tool integration for prerequisites validation

### Task Tool Integration Features
Each implementation guide now includes **Task tool integration** for autonomous execution of research-intensive phases:

- **Option A: Manual Workflow** (2-4 hours) - Traditional step-by-step approach
- **Option B: Task-Delegated Workflow** (30 min setup + autonomous execution) - AI agent handles research while you work on other components

**Benefits:**
- **Quality Multiplier**: More thorough research than manual approach
- **Time Savings**: Parallel work while Task researches autonomously
- **Better Coverage**: Comprehensive analysis across multiple sources
- **Flexibility**: Manual fallback always available

### Additional Resources
- **Requirements Documentation**: `claude_guide/snowflake_requirements/` - Feature requirements and specifications  
- **Extension Examples**: Study existing implementations in `src/main/java/liquibase/` for patterns

---
**Focus**: Enhancing the liquibase-snowflake extension with comprehensive Snowflake feature support.

