# AIPL Programs for Liquibase Extension Development

This directory contains AI Programming Language (AIPL) programs that provide immediately actionable, AI-consumable guidance for Liquibase extension development tasks.

## What are AIPL Programs?

AIPL programs are YAML-based executable guides that:
- Provide step-by-step commands and validations
- Include templates for code generation
- Offer conditional logic for different scenarios
- Enable AI systems to execute complex workflows autonomously

## Available Programs

### 1. `incomplete-implementation-detection.yaml`
**Purpose**: Systematic detection and resolution of incomplete Liquibase extension implementations

**Use Cases**:
- Detecting placeholder comments in Change classes
- Identifying missing getter/setter methods
- Finding incomplete `generateStatements()` implementations
- Validating SQL generator attribute processing

**Key Features**:
- Automated code pattern detection
- Template generation for missing methods
- Layer-by-layer validation (Change → Statement → SQL Generator)

### 2. `test-harness-orphaned-files.yaml`
**Purpose**: Audit and resolve orphaned test harness files

**Use Cases**:
- Finding XML files without corresponding SQL/JSON files
- Creating missing test harness components
- Validating test file triads (XML + SQL + JSON)
- Comprehensive test coverage auditing

**Key Features**:
- File comparison utilities
- Template-driven file creation
- Test execution validation

### 3. `systematic-implementation-debugging.yaml`
**Purpose**: Layer-by-layer debugging of Liquibase extension implementations

**Use Cases**:
- Tracing attribute flow through implementation layers
- Isolating failure points in the implementation stack
- Generating targeted fix templates
- SQL syntax validation

**Key Features**:
- Three-layer debugging methodology
- Failure point isolation logic
- Automated fix generation

### 4. `implementation-completeness-validation.yaml`
**Purpose**: Comprehensive validation of Liquibase extension implementation completeness

**Use Cases**:
- Pre-commit validation checks
- Implementation quality gates
- Comprehensive test suite execution
- Completeness reporting

**Key Features**:
- Multi-phase validation workflow
- Blocking checkpoints for critical issues
- Automated report generation

## How to Use AIPL Programs

### Variables
Each program uses variables for parameterization. Common variables include:
- `${LIQUIBASE_EXTENSION_ROOT}` - Path to extension source
- `${LIQUIBASE_TEST_HARNESS_ROOT}` - Path to test harness
- `${CHANGE_CLASS_NAME}` - Name of Change class
- `${DB_NAME}` - Database type (e.g., "snowflake")

### Execution Flow
1. **PHASES**: Sequential execution blocks
2. **STEPS**: Individual commands, validations, or file operations
3. **TEMPLATES**: Code generation patterns
4. **ERROR_HANDLING**: Failure response protocols

### Example Usage Context
These programs were developed during the resolution of:
- Incomplete `alterFileFormat` implementation in Liquibase Snowflake Extension
- Orphaned test harness files requiring SQL/JSON companions
- SQL generation spacing bugs (e.g., `SETCOMMENT` vs `SET COMMENT`)

## Integration with Implementation Guides

These AIPL programs complement the existing implementation guides:
- **Changetype Implementation**: Use with `../CHANGETYPE_IMPLEMENTATION_GUIDE.md`
- **Snapshot/Diff Implementation**: Use with `../SNAPSHOT_DIFF_IMPLEMENTATION_GUIDE.md`

## Content Rules Compliance

All programs follow `/Users/kevinchappell/Documents/GitHub/liquibase/claude_guide/AIPL/AI_CONTENT_RULES_BASIC.yaml`:
- YAML blocks, not prose
- Immediately actionable commands
- Template-driven code generation
- Boolean conditions with clear branching
- Copy-paste ready shell commands

## Future Development

These programs serve as patterns for creating additional AIPL automation for:
- XSD schema integration workflows
- Performance optimization validation
- Security compliance checking
- Cross-database compatibility testing