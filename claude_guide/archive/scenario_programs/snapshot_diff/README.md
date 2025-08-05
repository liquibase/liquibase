# Snapshot/Diff Implementation AIPL Programs

This directory contains scenario-specific AIPL programs for implementing snapshot and diff functionality for Snowflake database objects. Each program follows AIPL 2.0 standards and provides complete, autonomous execution workflows.

## 🎯 Program Selection Guide

### Scenario A: New Object Implementation
**File**: `new-object-implementation.yaml`
**Use When**: Implementing snapshot/diff for completely new database object
**Duration**: 2-4 hours (45min research + 8-12 AI-TDD micro-cycles)
**Output**: Complete object model, snapshot generator, diff comparator with tests

### Scenario B: Enhance Existing Object  
**File**: `enhance-existing-object.yaml`
**Use When**: Adding properties or capabilities to existing implementation
**Duration**: 1-2 hours (4-6 micro-cycles for new properties)
**Output**: Enhanced implementation with new properties and comprehensive tests

### Scenario C: Complete Incomplete Implementation
**File**: `complete-incomplete-implementation.yaml` 
**Use When**: Completing partial implementation with missing components
**Duration**: 1-3 hours (2-8 micro-cycles depending on missing pieces)
**Output**: Complete implementation with all components functional

### Scenario D: Fix Bugs in Implementation
**File**: `fix-bugs-implementation.yaml`
**Use When**: Systematic bug fixing in existing implementation
**Duration**: 30min-2 hours (1-4 micro-cycles per bug)
**Output**: Bug-free implementation with all tests passing

### Scenario E: Performance Optimization
**File**: `performance-optimization.yaml`
**Use When**: Optimizing performance of working implementation  
**Duration**: 1-3 hours (2-6 micro-cycles with performance tests)
**Output**: Optimized implementation with measurable performance improvements

## 🏗️ AIPL Architecture Integration

All programs follow the new AIPL 2.0 reference-based architecture:

### Core Standards (Always Referenced)
```yaml
IMPORTS:
  - "../../../AIPL/core/TERMINOLOGY_DEFINITIONS.yaml"
  - "../../../AIPL/core/STEP_SPECIFICATIONS.yaml" 
  - "../../../AIPL/core/AUTONOMOUS_OPERATION.yaml"
  - "../../../AIPL/libraries/snowflake/SNOWFLAKE_SNAPSHOT_DIFF_PATTERNS.yaml"
```

### Universal Standards Applied
- **BEHAVIORAL_KEYWORDS**: All programs use ACCEPTANCE_CRITERIA, QUALITY_GATE, SUCCESS_CRITERIA
- **STEP_CLASSIFICATION**: All steps follow 5 types: RESEARCH, PROCESSING, CREATION, VALIDATION, EXECUTION
- **UNIVERSAL_STEP_TEMPLATE**: All steps have complete specifications
- **AUTONOMOUS_OPERATION**: All programs include PHASE_0_5_AUTONOMOUS_PREREQUISITES

## 🚀 Quick Start

1. **Choose Your Scenario**: Select the appropriate AIPL program based on your situation
2. **Set Variables**: Update `OBJECT_TYPE` and other variables for your specific object
3. **Execute**: Run the AIPL program with autonomous operation enabled
4. **Monitor**: Track progress through the defined phases and steps

## 📋 Common Variables

All programs use these standard variables (customize as needed):

```yaml
VARIABLES:
  OBJECT_TYPE: "${TARGET_OBJECT_TYPE}"        # e.g., "FileFormat", "Warehouse"
  OBJECT_TYPE_LOWER: "fileformat"             # Lowercase version
  PROJECT_BASE: "liquibase-snowflake"         # Project directory
  PACKAGE_BASE: "liquibase.ext.snowflake"     # Java package base
  TEST_PACKAGE: "liquibase.ext.snowflake"     # Test package base
```

## 🔧 Autonomous Operation Requirements

All programs require autonomous operation capability:

### Required CLAUDE.md Patterns
```bash
# AI-TDD Micro-Cycle Test Commands (Autonomous Operation Enabled)
mvn test -Dtest="*${OBJECT_TYPE}*Test*" -q
mvn test -Dtest="${TEST_CLASS}" -q
mvn test -Dtest="${TEST_CLASS}#${TEST_METHOD}" -q
mvn compile -q
```

### Validation Commands
- `grep -c 'mvn test.*-Dtest=' CLAUDE.md` should return ≥ 3
- `test -r CLAUDE.md` should succeed

## 🎯 Success Criteria

All programs achieve these standard outcomes:

### Implementation Completeness
- ✅ Complete object model with all required properties
- ✅ Functional snapshot generator with database introspection  
- ✅ Working diff comparator with property comparison
- ✅ Comprehensive test coverage for all components
- ✅ Proper service registration with Liquibase

### Quality Validation
- ✅ All tests pass consistently
- ✅ Code compiles without errors or warnings
- ✅ Integration with Liquibase system verified
- ✅ End-to-end functionality working correctly

### Autonomous Operation
- ✅ Implementation completed using autonomous test execution
- ✅ All command patterns approved for autonomous operation
- ✅ Test-driven development cycle successfully executed

## 🔗 Related Resources

### AIPL Standards
- **Core Standards**: `../../../AIPL/core/`
- **Implementation Approaches**: `../../../AIPL/approaches/`
- **Language Specification**: `../../../AIPL/language/`

### Domain Library
- **Snowflake Patterns**: `../../../AIPL/libraries/snowflake/SNOWFLAKE_SNAPSHOT_DIFF_PATTERNS.yaml`

### Supporting Programs
- **Systematic Debugging**: `../../troubleshooting/systematic-debugging.yaml`
- **Incomplete Detection**: `../../troubleshooting/incomplete-detection.yaml`
- **Integration Testing**: `../../troubleshooting/validation_programs/snapshot-diff-integration-testing.yaml`

## 📊 Expected Results

### Time Investment vs. Complexity
| Scenario | Research | Implementation | Testing | Total |
|----------|----------|----------------|---------|-------|
| New Object | 45 min | 90-120 min | 30 min | 2-4 hours |
| Enhance Existing | 15 min | 30-60 min | 15 min | 1-2 hours |
| Complete Incomplete | 30 min | 45-90 min | 15 min | 1-3 hours |
| Fix Bugs | 15 min | 15-90 min | 15 min | 30min-2 hours |
| Performance Optimization | 30 min | 60-120 min | 30 min | 1-3 hours |

### Quality Metrics
- **Test Coverage**: ≥ 90% line coverage
- **Performance**: Baseline or better performance
- **Maintainability**: Clean, readable, documented code
- **Integration**: Seamless Liquibase integration

---

**Note**: These programs replace the complex 1,878-line monolithic guide with focused, executable workflows. Each program is self-contained and follows AIPL standards for immediate actionability and autonomous operation.