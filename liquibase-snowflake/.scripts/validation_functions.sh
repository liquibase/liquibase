#!/bin/bash
# Core validation functions for state-driven TDD enforcement
# Based on original SNAPSHOT_DIFF_IMPLEMENTATION_GUIDE.md quality gates

set -e

# Phase validation functions
validate_phase_transition() {
    local from_phase="$1"
    local to_phase="$2"
    
    echo "Validating phase transition: $from_phase -> $to_phase"
    
    case "$to_phase" in
        "requirements_research")
            # Can always start with requirements research
            return 0
            ;;
        "tdd_object_model")
            test -f ".checkpoints/requirements_complete" || {
                echo "BLOCKED: Requirements phase not complete"
                echo "Missing: .checkpoints/requirements_complete"
                return 1
            }
            ;;
        "snapshot_generator")
            test -f ".checkpoints/object_model_complete" || {
                echo "BLOCKED: Object model phase not complete"
                return 1
            }
            ;;
        "diff_comparator")
            test -f ".checkpoints/snapshot_generator_complete" || {
                echo "BLOCKED: Snapshot generator phase not complete"
                return 1
            }
            ;;
        "integration")
            test -f ".checkpoints/diff_comparator_complete" || {
                echo "BLOCKED: Diff comparator phase not complete"
                return 1
            }
            ;;
        *)
            echo "ERROR: Unknown phase: $to_phase"
            return 1
            ;;
    esac
}

# Requirements validation (from original guide criteria)
validate_requirements_document() {
    local req_file="$1"
    local object_type="$2"
    
    echo "Validating requirements document: $req_file"
    
    # Must exist
    test -f "$req_file" || {
        echo "FAILED: Requirements document not found: $req_file"
        return 1
    }
    
    # Must have minimum property count (original guide: 15+)
    local prop_count=$(grep -c "REQUIRED\|OPTIONAL" "$req_file" 2>/dev/null || echo "0")
    prop_count=$(echo "$prop_count" | tr -d '\n')
    if [ "$prop_count" -lt 15 ]; then
        echo "FAILED: Only $prop_count properties documented, need 15+"
        echo "Current properties found:"
        grep "REQUIRED\|OPTIONAL" "$req_file" | head -5
        return 1
    fi
    
    # Must have official Snowflake documentation URLs (original guide: 3+)
    local url_count=$(grep -c "docs.snowflake.com" "$req_file" 2>/dev/null || echo "0")
    if [ "$url_count" -lt 3 ]; then
        echo "FAILED: Only $url_count Snowflake URLs found, need 3+"
        echo "Expected: CREATE, SHOW, DESCRIBE documentation URLs"
        return 1
    fi
    
    # Must have SQL query specifications
    local sql_count=$(grep -c "SELECT\|SHOW\|DESCRIBE" "$req_file" 2>/dev/null || echo "0")
    if [ "$sql_count" -lt 2 ]; then
        echo "FAILED: Only $sql_count SQL queries found, need 2+"
        echo "Expected: SHOW and DESCRIBE queries for introspection"
        return 1
    fi
    
    # Must have property analysis table
    grep -q "Property.*Type.*Required" "$req_file" || {
        echo "FAILED: Missing property analysis table"
        echo "Expected format: | Property | Type | Required | Default | Comparison | Notes |"
        return 1
    }
    
    echo "✅ Requirements validation PASSED"
    echo "  - Properties documented: $prop_count"
    echo "  - Official URLs: $url_count"
    echo "  - SQL queries: $sql_count"
    echo "  - Property analysis table: present"
    
    return 0
}

# Enhanced template generation validation
validate_template_generation_success() {
    local object_type="$1"
    local base_package="src/main/java/liquibase"
    
    echo "Validating template generation success for: $object_type"
    
    # Validate package declarations match directory structure
    local object_file="${base_package}/database/object/${object_type}.java"
    test -f "$object_file" || {
        echo "FAILED: Object file not found: $object_file"
        return 1
    }
    
    grep -q "package liquibase.database.object;" "$object_file" || {
        echo "FAILED: Package declaration doesn't match directory structure"
        echo "Expected: package liquibase.database.object;"
        echo "Found: $(grep '^package' "$object_file")"
        return 1
    }
    
    # Validate no template placeholders remain
    local placeholder_count=$(grep -c '\${.*}' "$object_file" 2>/dev/null || echo "0")
    placeholder_count=$(echo "$placeholder_count" | tr -d '\n')
    if [ "$placeholder_count" -gt 0 ]; then
        echo "FAILED: $placeholder_count unresolved template placeholders"
        echo "Remaining placeholders:"
        grep '\${.*}' "$object_file"
        return 1
    fi
    
    # Immediate compilation check
    echo "Testing compilation of generated templates..."
    mvn compile -q || {
        echo "FAILED: Template generated non-compiling code"
        return 1
    }
    
    echo "✅ Template generation validation PASSED"
    return 0
}

# Framework integration compliance validation
validate_framework_integration_compliance() {
    local object_type="$1"
    local object_file="src/main/java/liquibase/database/object/${object_type}.java"
    
    echo "Validating framework integration compliance for: $object_type"
    
    # Validate AbstractDatabaseObject extension
    grep -q "extends AbstractDatabaseObject" "$object_file" || {
        echo "FAILED: Must extend AbstractDatabaseObject"
        echo "Found: $(grep 'extends' "$object_file" || echo 'No extends clause')"
        return 1
    }
    
    # Validate required method signatures exactly
    grep -q "public.*getName()" "$object_file" || {
        echo "FAILED: Missing public getName() method"
        return 1
    }
    
    # Validate setName returns correct type
    grep -q "public.*${object_type}.*setName(" "$object_file" || {
        echo "FAILED: setName must return ${object_type} type for method chaining"
        echo "Found: $(grep 'setName(' "$object_file" || echo 'No setName method')"
        return 1
    }
    
    # Validate getContainingObjects implementation
    grep -q "getContainingObjects()" "$object_file" || {
        echo "FAILED: Missing getContainingObjects() implementation"
        return 1
    }
    
    # Validate equals and hashCode methods present
    grep -q "public boolean equals(" "$object_file" || {
        echo "FAILED: Missing equals() method implementation"
        return 1
    }
    
    grep -q "public int hashCode(" "$object_file" || {
        echo "FAILED: Missing hashCode() method implementation"
        return 1
    }
    
    echo "✅ Framework integration compliance PASSED"
    return 0
}

# Property pattern validation
validate_property_patterns() {
    local object_type="$1"
    local object_file="src/main/java/liquibase/database/object/${object_type}.java"
    
    echo "Validating property patterns for: $object_type"
    
    # Validate getter/setter pairs (excluding getName/setName which are special)
    local getters=$(grep -c "public.*get[A-Z]" "$object_file" 2>/dev/null || echo "0")
    local setters=$(grep -c "public.*set[A-Z]" "$object_file" 2>/dev/null || echo "0")
    
    echo "  - Found $getters getters, $setters setters"
    
    # Validate setter return types for method chaining (exclude setName which we already validated)
    local bad_setters=$(grep -E "public void set[A-Z]" "$object_file" 2>/dev/null | grep -v setName | wc -l || echo "0")
    if [ "$bad_setters" -gt 0 ]; then
        echo "FAILED: $bad_setters setters return void instead of $object_type"
        echo "All setters must return $object_type for method chaining"
        echo "Bad setters:"
        grep -E "public void set[A-Z]" "$object_file" | grep -v setName
        return 1
    fi
    
    echo "✅ Property patterns validation PASSED"
    return 0
}

# TDD micro-cycle validation (original guide Red-Green-Refactor enforcement)  
validate_red_phase() {
    local test_class="$1"
    local test_method="$2"
    
    echo "Validating RED phase: Test must fail first"
    
    # Run the specific test
    mvn test -Dtest="${test_class}#${test_method}" -q
    local exit_code=$?
    
    # Test MUST fail in red phase (original guide requirement)
    if [ $exit_code -eq 0 ]; then
        echo "FAILED: Test passed immediately - invalid RED phase"
        echo "Red phase requires failing test to drive implementation"
        return 1
    fi
    
    echo "✅ RED phase validation PASSED - test fails as expected"
    return 0
}

validate_green_phase() {
    local test_class="$1"
    local test_method="$2"
    
    echo "Validating GREEN phase: Test must pass after implementation"
    
    # Run the specific test
    mvn test -Dtest="${test_class}#${test_method}" -q
    local exit_code=$?
    
    # Test MUST pass in green phase
    if [ $exit_code -ne 0 ]; then
        echo "FAILED: Test still fails after implementation"
        echo "Green phase requires minimal implementation to make test pass"
        return 1
    fi
    
    echo "✅ GREEN phase validation PASSED - test passes"
    return 0
}

validate_refactor_phase() {
    local test_class="$1"
    
    echo "Validating REFACTOR phase: All tests must still pass"
    
    # Run all tests in the class
    mvn test -Dtest="${test_class}" -q
    local exit_code=$?
    
    # All tests MUST pass after refactoring
    if [ $exit_code -ne 0 ]; then
        echo "FAILED: Refactoring broke existing tests"
        echo "Refactor phase must not change behavior - revert changes"
        return 1
    fi
    
    echo "✅ REFACTOR phase validation PASSED - all tests pass"
    return 0
}

# Object model validation (original guide completeness criteria)
validate_object_model() {
    local object_type="$1"
    local object_file="src/main/java/liquibase/ext/snowflake/database/${object_type}.java"
    
    echo "Validating object model: $object_file"
    
    # Must exist and compile
    test -f "$object_file" || {
        echo "FAILED: Object model file not found: $object_file"
        return 1
    }
    
    # Must compile successfully
    mvn compile -q || {
        echo "FAILED: Object model does not compile"
        return 1
    }
    
    # Must extend AbstractDatabaseObject (original guide requirement)
    grep -q "extends AbstractDatabaseObject" "$object_file" || {
        echo "FAILED: Must extend AbstractDatabaseObject"
        return 1
    }
    
    # Must implement required methods (original guide pattern)
    grep -q "getName()" "$object_file" || {
        echo "FAILED: Missing getName() method"
        return 1
    }
    
    grep -q "setName(" "$object_file" || {
        echo "FAILED: Missing setName() method" 
        return 1
    }
    
    grep -q "getSchema()" "$object_file" || {
        echo "FAILED: Missing getSchema() method"
        return 1
    }
    
    # Must have equals and hashCode (original guide requirement)
    grep -q "equals(" "$object_file" || {
        echo "FAILED: Missing equals() method"
        return 1
    }
    
    grep -q "hashCode()" "$object_file" || {
        echo "FAILED: Missing hashCode() method"
        return 1
    }
    
    # Count property methods (getter/setter pairs)
    local getter_count=$(grep -c "get[A-Z].*(" "$object_file")
    local setter_count=$(grep -c "set[A-Z].*(" "$object_file")
    
    if [ "$getter_count" -lt 10 ] || [ "$setter_count" -lt 10 ]; then
        echo "FAILED: Insufficient property methods"
        echo "  Getters: $getter_count, Setters: $setter_count (need 10+ each)"
        return 1
    fi
    
    echo "✅ Object model validation PASSED"
    echo "  - File exists and compiles"
    echo "  - Extends AbstractDatabaseObject"
    echo "  - Has required methods (getName, setName, getSchema)"
    echo "  - Has equals/hashCode implementation"
    echo "  - Property methods: $getter_count getters, $setter_count setters"
    
    return 0
}

# Test validation (original guide test coverage requirements)
validate_test_suite() {
    local test_class="$1"
    local test_file="src/test/java/liquibase/ext/snowflake/database/${test_class}Test.java"
    
    echo "Validating test suite: $test_file"
    
    # Must exist
    test -f "$test_file" || {
        echo "FAILED: Test file not found: $test_file"
        return 1
    }
    
    # Must have minimum test count (original guide: systematic test categories)
    local test_count=$(grep -c "@Test" "$test_file")
    if [ "$test_count" -lt 8 ]; then
        echo "FAILED: Only $test_count tests found, need 8+"
        echo "Expected: positive, negative, boundary, edge case tests"
        return 1
    fi
    
    # All tests must pass
    mvn test -Dtest="${test_class}Test" -q || {
        echo "FAILED: Some tests are failing"
        return 1
    }
    
    echo "✅ Test suite validation PASSED"
    echo "  - Test count: $test_count"
    echo "  - All tests passing"
    
    return 0
}

# Service registration validation (original guide integration requirement)
validate_service_registration() {
    local object_type="$1"
    local snapshot_service="src/main/resources/META-INF/services/liquibase.snapshot.SnapshotGenerator"
    local comparator_service="src/main/resources/META-INF/services/liquibase.diff.compare.DatabaseObjectComparator"
    
    echo "Validating service registration for $object_type"
    
    # Check snapshot generator registration
    if [ -f "$snapshot_service" ]; then
        grep -q "${object_type}SnapshotGenerator" "$snapshot_service" || {
            echo "FAILED: ${object_type}SnapshotGenerator not registered in $snapshot_service"
            return 1
        }
    else
        echo "FAILED: Snapshot service file not found: $snapshot_service"
        return 1
    fi
    
    # Check comparator registration  
    if [ -f "$comparator_service" ]; then
        grep -q "${object_type}Comparator" "$comparator_service" || {
            echo "FAILED: ${object_type}Comparator not registered in $comparator_service"
            return 1
        }
    else
        echo "FAILED: Comparator service file not found: $comparator_service"
        return 1
    fi
    
    echo "✅ Service registration validation PASSED"
    echo "  - SnapshotGenerator registered"
    echo "  - Comparator registered"
    
    return 0
}

# Utility functions
get_current_phase() {
    cat ".process_state/current_phase" 2>/dev/null || echo "unknown"
}

get_target_object() {
    cat ".process_state/target_object" 2>/dev/null || echo "unknown"
}

set_phase() {
    local phase="$1"
    echo "$phase" > ".process_state/current_phase"
    echo "Phase set to: $phase"
}

create_checkpoint() {
    local checkpoint="$1"
    touch ".checkpoints/$checkpoint"
    echo "Checkpoint created: $checkpoint"
}

check_checkpoint() {
    local checkpoint="$1"
    test -f ".checkpoints/$checkpoint"
}

echo "Validation functions loaded successfully"