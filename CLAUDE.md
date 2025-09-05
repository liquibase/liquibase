# CLAUDE.md

This file provides high-level guidance to Claude Code when working on the Liquibase Snowflake Extension.

## 🎯 MILESTONE ACHIEVED: 95%+ Coverage + Complete Unified Extensibility ✅

**STATUS**: Major breakthrough achieved (August 2025)
- ✅ **95%+ Test Coverage**: Comprehensive snapshot generator coverage through systematic enhancement
- ✅ **Unified Extensibility Framework**: Extension → extension addsTo() relationships solved for account-level objects
- ✅ **Complete Schema Operations**: FileFormat, Warehouse, Database, Schema operations fully implemented  
- ✅ **Advanced Testing Patterns**: Complete SQL string assertions, MockedStatic chains, resource cleanup verification
- ✅ **Extension Object Architecture**: Account-level (Warehouse) vs schema-level (FileFormat) patterns established

Working on: Advanced Snowflake extension development
Validation: Simple INFORMATION_SCHEMA + unified framework (replaced complex approaches)

## 🔧 UNIFIED EXTENSIBILITY FRAMEWORK BREAKTHROUGH

### ⚡ Major Architectural Achievement (2025-08-07)
**SOLVED**: Extension → extension `addsTo()` relationships for account-level objects (Warehouse, User, Role)

**Status**: ✅ **VALIDATED** - SnowflakeExtensionDiffGeneratorSimple.java operational
**Impact**: 10 warehouses discovered reliably, Account objects working, tests passing

**Implementation Details**: See `SNAPSHOT_DIFF_IMPLEMENTATION_GUIDE.md` for complete extension object patterns and implementation decisions.

## ⚡ Integration Test Commands (95%+ Coverage Achievement Pattern)

### MILESTONE ACHIEVED: Comprehensive Test Commands
```bash
# Snapshot Generators (95%+ Coverage Achieved - Complete SQL String Assertions)
mvn test -Dtest="*SnapshotGenerator*Test*" -q

# FileFormat Complete Testing (MILESTONE ACHIEVED)
mvn test -Dtest="*FileFormat*Test*" -q
mvn test -Dtest="FileFormatSnapshotGeneratorTest" -q  
mvn test -Dtest="FileFormatComparatorTest" -q

# Warehouse Complete Testing (MILESTONE ACHIEVED + Unified Framework)  
mvn test -Dtest="*Warehouse*Test*" -q
mvn test -Dtest="WarehouseSnapshotGeneratorTest" -q
mvn test -Dtest="WarehouseSnapshotIsolationTest" -q

# Schema Operations Testing (MILESTONE ACHIEVED)
mvn test -Dtest="*Schema*Test*" -q
mvn test -Dtest="CreateSchemaGeneratorSnowflakeNamespaceTest" -q

# UniqueConstraint Testing (Bug Fixed)
mvn test -Dtest="UniqueConstraintSnapshotGeneratorSnowflakeTest" -q

# Unified Extensibility Framework Testing
mvn test -Dtest="WarehouseDiffIntegrationTest" -q

# Full-Lifecycle Integration Tests (End-to-End Validation)
mvn test -Dtest="*FullCycle*Test" -q                   # All full-cycle tests
mvn test -Dtest="WarehouseFullCycleIntegrationTest" -q  # Warehouse end-to-end
mvn test -Dtest="FileFormatFullCycleIntegrationTest" -q # FileFormat end-to-end
mvn test -Dtest="DatabaseFullCycleIntegrationTest" -q   # Database end-to-end

# Test Coverage Analysis (Post-Enhancement)
mvn test jacoco:report
open target/site/jacoco/index.html
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

### Implementation Status (August 2025)
- ✅ **Unified Extensibility Framework**: Account-level objects (Warehouse) working via SnowflakeExtensionDiffGeneratorSimple
- ✅ **95%+ Test Coverage**: Systematic enhancement of snapshot generators with complete SQL string assertions
- ✅ **Advanced Testing Patterns**: MockedStatic chains, resource cleanup verification, large dataset testing
- ✅ **Extension Object Architecture**: Schema-level vs account-level patterns established and documented
- ✅ **Complete CRUD Operations**: All major objects support CREATE/ALTER/DROP with comprehensive properties
- ✅ **Database Introspection**: Full snapshot/diff capability for schema comparison and changelog generation

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
- **Snapshot Generators**: `src/main/java/liquibase/snapshot/jvm/` - Database introspection components
- **Test Coverage**: 80%+ achieved with comprehensive test suites for all major components

## 📚 Essential Implementation Guides

### 🎯 AI-Optimized Single-Source Guides
Each guide is **fully self-contained** with no document hunting required. Guides are **cross-linked** to eliminate duplication:

1. **Complete Changetype Implementation** 
   - **File**: `claude_guide/implementation_guides/CHANGETYPE_IMPLEMENTATION_GUIDE.md`
   - **Scope**: Database operations (CREATE/ALTER/DROP), namespace attribute extensions, XSD schema integration
   
2. **Complete Snapshot/Diff Implementation**
   - **File**: `claude_guide/implementation_guides/SNAPSHOT_DIFF_IMPLEMENTATION_GUIDE.md` 
   - **Scope**: Database introspection, extension object patterns, 95%+ coverage workflows
   - **Contains**: Schema-level vs account-level implementation decisions, unified extensibility framework
   
3. **Complete Diff/Generate Changelog**
   - **File**: `claude_guide/implementation_guides/DIFF_GENERATE_CHANGELOG_IMPLEMENTATION_GUIDE.md`
   - **Scope**: Schema comparison, changelog generation, prerequisites validation, integration testing

### 📋 Implementation Workflow
```mermaid
CHANGETYPE_GUIDE → SNAPSHOT_DIFF_GUIDE → DIFF_CHANGELOG_GUIDE → VALIDATION
     ↓                    ↓                      ↓                  ↓
Database Ops      Introspection        Schema Comparison      End-to-End
(CREATE/ALTER)    (Object Discovery)   (Changelog Gen)        (Testing)
```

### 🔧 Task Tool Integration
**All guides support**: Manual workflow (2-4 hours) OR Task-delegated research (30 min setup + autonomous execution)

### ⚡ Quick Reference Commands
```bash
# Extension object validation (unified framework)
mvn test -Dtest="WarehouseSnapshotIsolationTest" -q

# 95%+ coverage workflow
mvn test jacoco:report && open target/site/jacoco/index.html

# Architectural compliance check
find . -name '*ChangeSnowflake.java' -path '*/change/core/*'  # Should be empty
```

---

## 🚨 **CRITICAL DOCUMENTATION VALIDATION PATTERNS (Added from Session)**

### **Essential Before ANY User Documentation**
**LEARNED**: Documentation errors cause immediate user trust loss and frustration. Must validate systematically.

### **Systematic Documentation Validation Commands**
```bash
# Complete inventory extraction (Phase 1)
grep -n "snowflake:[a-zA-Z]*" docs/*.md | head -100    # All XML elements
grep -n "[a-zA-Z]*=\"[^\"]*\"" docs/*.md | head -100   # All XML attributes
grep -n "liquibase [a-zA-Z-]*" docs/*.md               # All commands

# Cross-reference validation (Phase 2) - CRITICAL STEP
# For each attribute found, verify against Java source:
grep -n "@DatabaseChangeProperty.*ATTRIBUTE_NAME" src/main/java/liquibase/change/core/*.java
```

### **Project-Specific Error Patterns to Catch**
```bash
# Invalid "set" prefixes (ALTER operations use base attribute names)
grep -n "set[A-Z][a-zA-Z]*=" docs/*.md

# Data type documentation errors (common pattern)
grep -n "dataRetentionTimeInDays.*Integer" docs/*.md    # Should be String in Java

# Non-existent clone attributes (common copy-paste error)
grep -n "cloneFromDatabase\|cloneFromSchema" docs/*.md  # Should be cloneFrom/fromSchema

# XML syntax errors (quote escaping)
grep -n 'fieldOptionallyEnclosedBy='"'"'"' docs/*.md    # Should use &quot;

# Incorrect validation ranges
grep -n "autoSuspend.*60.*86400" docs/*.md             # Missing 0 (never) option
```

### **Data Type Validation Against Java Source**
```yaml
CRITICAL_MAPPINGS:
  dataRetentionTimeInDays: "String (not Integer)"
  maxDataExtensionTimeInDays: "String (not Integer)"  
  startValue: "BigInteger (not Long)"
  incrementBy: "BigInteger (not Long)"
  minValue: "BigInteger (not Long)"
  maxValue: "BigInteger (not Long)"
  autoSuspend: "Integer (allows 0 = never suspend)"
```

### **Systematic Validation Workflow**
```bash
# Step 1: Extract complete inventory
cd docs/
grep -rn "snowflake:" . > /tmp/xml_elements.txt
grep -rn "liquibase " . > /tmp/commands.txt

# Step 2: Cross-reference EVERY item against source code
# Step 3: Fix ALL errors before user documentation release
```

---
**Focus**: Enhancing the liquibase-snowflake extension with comprehensive Snowflake feature support.

