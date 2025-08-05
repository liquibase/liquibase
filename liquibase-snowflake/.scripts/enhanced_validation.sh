#!/bin/bash
# Enhanced validation functions integrating with legacy guide patterns
# Implements the complete 4-category testing validation

set -e
source .scripts/validation_functions.sh

# Enhanced test coverage validation based on legacy guide
validate_comprehensive_test_coverage() {
    local test_class="$1"
    local test_file="src/test/java/liquibase/ext/snowflake/database/${test_class}Test.java"
    
    echo "Validating comprehensive test coverage for: $test_class"
    
    # Must exist
    test -f "$test_file" || {
        echo "FAILED: Test file not found: $test_file"
        return 1
    }
    
    # Check for 4-category test structure (legacy guide requirement)
    local positive_tests=$(grep -c "// === POSITIVE TESTS ===" "$test_file" 2>/dev/null || echo "0")
    local negative_tests=$(grep -c "// === NEGATIVE TESTS ===" "$test_file" 2>/dev/null || echo "0")
    local boundary_tests=$(grep -c "// === BOUNDARY TESTS ===" "$test_file" 2>/dev/null || echo "0")
    local edge_tests=$(grep -c "// === EDGE CASE TESTS ===" "$test_file" 2>/dev/null || echo "0")
    
    if [ "$positive_tests" -eq 0 ] || [ "$negative_tests" -eq 0 ] || [ "$boundary_tests" -eq 0 ] || [ "$edge_tests" -eq 0 ]; then
        echo "FAILED: Missing systematic test categories"
        echo "  Expected: POSITIVE, NEGATIVE, BOUNDARY, EDGE CASE sections"
        echo "  Found: Positive:$positive_tests, Negative:$negative_tests, Boundary:$boundary_tests, Edge:$edge_tests"
        return 1
    fi
    
    # Minimum test count per category (legacy guide: 15+ total tests)
    local total_test_count=$(grep -c "@Test" "$test_file")
    if [ "$total_test_count" -lt 15 ]; then
        echo "FAILED: Only $total_test_count tests found, need 15+ for comprehensive coverage"
        return 1
    fi
    
    # All tests must pass
    mvn test -Dtest="${test_class}Test" -q || {
        echo "FAILED: Some tests are failing"
        return 1
    }
    
    echo "✅ Comprehensive test coverage validation PASSED"
    echo "  - 4-category structure: present"
    echo "  - Total test count: $total_test_count"
    echo "  - All tests passing: yes"
    
    return 0
}

# Validate 8 micro-cycle completion pattern
validate_8_microcycle_completion() {
    local object_type="$1"
    local cycle_count=$(cat .process_state/micro_cycle_count 2>/dev/null || echo "0")
    
    echo "Validating 8 micro-cycle completion pattern"
    
    # Must have completed at least 8 micro-cycles (legacy guide proven pattern)
    if [ "$cycle_count" -lt 8 ]; then
        echo "FAILED: Only $cycle_count micro-cycles completed, need minimum 8"
        echo "Legacy guide proven pattern: 8 micro-cycles = complete implementation"
        return 1
    fi
    
    # All required checkpoint files must exist
    local required_checkpoints=(
        "requirements_complete"
        "object_model_complete" 
        "snapshot_generator_complete"
        "diff_comparator_complete"
    )
    
    for checkpoint in "${required_checkpoints[@]}"; do
        test -f ".checkpoints/$checkpoint" || {
            echo "FAILED: Missing required checkpoint: $checkpoint"
            return 1
        }
    done
    
    echo "✅ 8 micro-cycle completion validation PASSED"
    echo "  - Micro-cycles completed: $cycle_count"
    echo "  - All phase checkpoints: present"
    
    return 0  
}

# Complete template generation validation with framework integration
validate_complete_template_generation() {
    local object_type="$1"
    
    echo "🔧 ENHANCED TEMPLATE GENERATION VALIDATION"
    echo "Object Type: $object_type"
    
    # Step 1: Basic template generation success
    validate_template_generation_success "$object_type" || {
        echo "❌ Template generation failed"
        return 1
    }
    
    # Step 2: Framework integration compliance
    validate_framework_integration_compliance "$object_type" || {
        echo "❌ Framework integration compliance failed"
        return 1
    }
    
    # Step 3: Property patterns validation
    validate_property_patterns "$object_type" || {
        echo "❌ Property patterns validation failed"
        return 1
    }
    
    # Step 4: Test compilation validation
    echo "Testing test class compilation..."
    mvn test-compile -q || {
        echo "❌ Test classes do not compile"
        return 1
    }
    
    echo "✅ COMPLETE TEMPLATE GENERATION VALIDATION PASSED"
    echo "  - Package structure: correct"
    echo "  - Framework integration: compliant"
    echo "  - Property patterns: valid"
    echo "  - Compilation: successful"
    
    return 0
}

# Enhanced micro-cycle validation with framework integration checks
validate_micro_cycle_integration() {
    local test_class="$1"
    local test_method="$2"
    local object_type="$3"
    
    echo "🔄 ENHANCED MICRO-CYCLE VALIDATION"
    echo "Test: ${test_class}#${test_method}"
    echo "Object: $object_type"
    
    # Standard TDD validation
    validate_red_phase "$test_class" "$test_method" || return 1
    validate_green_phase "$test_class" "$test_method" || return 1
    
    # NEW: Framework integration validation after each cycle
    if [ -n "$object_type" ]; then
        validate_framework_integration_compliance "$object_type" || {
            echo "❌ Micro-cycle broke framework integration"
            return 1
        }
        
        # Property pattern validation after property additions
        validate_property_patterns "$object_type" || {
            echo "❌ Property patterns broken by micro-cycle"
            return 1
        }
    fi
    
    validate_refactor_phase "$test_class" || return 1
    
    echo "✅ ENHANCED MICRO-CYCLE VALIDATION PASSED"
    return 0
}

# Enhanced property validation (legacy guide: 20+ properties beyond identity)
validate_enhanced_property_requirements() {
    local req_file="$1"
    local object_type="$2"
    
    echo "Validating enhanced property requirements (legacy guide standards)"
    
    # Call base validation first
    validate_requirements_document "$req_file" "$object_type" || return 1
    
    # Legacy guide specific validations
    local total_properties=$(grep -c "REQUIRED\|OPTIONAL" "$req_file" 2>/dev/null || echo "0")
    local identity_properties=$(grep -c "IDENTITY" "$req_file" 2>/dev/null || echo "0")
    local config_properties=$(grep -c "PROPERTY" "$req_file" 2>/dev/null || echo "0")
    
    # Must have 20+ properties beyond identity (legacy guide requirement)
    total_properties=$(echo "$total_properties" | tr -d '\n')
    identity_properties=$(echo "$identity_properties" | tr -d '\n')
    local non_identity_count=$((total_properties - identity_properties))
    if [ "$non_identity_count" -lt 20 ]; then
        echo "FAILED: Only $non_identity_count non-identity properties, need 20+"
        echo "Legacy guide requirement: 20+ properties beyond identity properties"
        return 1
    fi
    
    # Must have domain-specific Snowflake values (legacy guide anti-generic validation)
    local snowflake_values=$(grep -c "CSV\|JSON\|PARQUET\|GZIP\|BROTLI\|AUTO\|TRUE\|FALSE" "$req_file" 2>/dev/null || echo "0")
    if [ "$snowflake_values" -lt 15 ]; then
        echo "FAILED: Only $snowflake_values Snowflake-specific values, need 15+"
        echo "Legacy guide requirement: Domain-specific Snowflake enums and values"
        return 1
    fi
    
    # Must not have generic properties (legacy guide anti-generic validation)
    local generic_count=$(grep -c "property[0-9]\|PROPERTY[0-9]\|genericProperty" "$req_file" 2>/dev/null || echo "0")
    if [ "$generic_count" -gt 0 ]; then
        echo "FAILED: Found $generic_count generic properties"
        echo "Legacy guide requirement: Zero generic properties allowed"
        return 1
    fi
    
    echo "✅ Enhanced property requirements validation PASSED"
    echo "  - Total properties: $total_properties"
    echo "  - Non-identity properties: $non_identity_count"
    echo "  - Snowflake-specific values: $snowflake_values"
    echo "  - Generic properties: $generic_count (must be 0)"
    
    return 0
}

# Validate XSD integration readiness
validate_xsd_integration_readiness() {
    local object_type="$1"
    
    echo "Validating XSD integration readiness"
    
    # Object model must compile and have proper structure
    local object_file="src/main/java/liquibase/ext/snowflake/database/${object_type}.java"
    test -f "$object_file" || {
        echo "FAILED: Object model not found: $object_file"
        return 1
    }
    
    # Must compile successfully
    mvn compile -q || {
        echo "FAILED: Object model compilation issues"
        return 1
    }
    
    # Check for changetype integration points
    local has_attributes=$(grep -c "setAttribute\|getAttribute" "$object_file" 2>/dev/null || echo "0")
    if [ "$has_attributes" -eq 0 ]; then
        echo "WARNING: No attribute methods found - may need changetype integration"
        echo "XSD integration requires setAttribute/getAttribute patterns"
    fi
    
    echo "✅ XSD integration readiness validation PASSED"
    echo "  - Object model exists and compiles"
    echo "  - Attribute methods: $has_attributes"
    
    return 0
}

echo "Enhanced validation functions loaded successfully"