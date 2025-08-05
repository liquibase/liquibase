#!/bin/bash
# State management for TDD workflow enforcement
# Implements state machine from original SNAPSHOT_DIFF_IMPLEMENTATION_GUIDE.md

set -e
source .scripts/validation_functions.sh
source .scripts/enhanced_validation.sh

# Phase transition with validation
transition_to_phase() {
    local new_phase="$1"
    local current_phase=$(get_current_phase)
    
    echo "Attempting phase transition: $current_phase -> $new_phase"
    
    # Validate transition is allowed
    validate_phase_transition "$current_phase" "$new_phase" || {
        echo "BLOCKED: Phase transition not allowed"
        echo "Complete current phase requirements first"
        return 1
    }
    
    # Set new phase
    set_phase "$new_phase"
    echo "$(date)" > ".process_state/phase_started"
    
    return 0
}

# Initialize TDD workflow for object type
initialize_workflow() {
    local object_type="$1"
    local scenario="${2:-NEW_OBJECT}"
    
    echo "Initializing TDD workflow for $object_type (scenario: $scenario)"
    
    # Set initial state
    echo "$object_type" > ".process_state/target_object"
    echo "$scenario" > ".process_state/scenario"
    echo "0" > ".process_state/micro_cycle_count"
    echo "$(date)" > ".process_state/workflow_started"
    
    # Clear any existing checkpoints
    rm -f .checkpoints/*
    
    # Set initial phase
    case "$scenario" in
        "NEW_OBJECT")
            set_phase "requirements_research"
            ;;
        "ENHANCE_EXISTING")
            set_phase "tdd_object_model"
            ;;
        "COMPLETE_INCOMPLETE")
            set_phase "incomplete_analysis"
            ;;
        "FIX_BUGS")
            set_phase "bug_analysis"
            ;;
        "OPTIMIZE_PERFORMANCE")
            set_phase "performance_analysis"
            ;;
        *)
            echo "ERROR: Unknown scenario: $scenario"
            return 1
            ;;
    esac
    
    echo "✅ Workflow initialized successfully"
    show_current_state
}

# Display current workflow state
show_current_state() {
    echo "=== CURRENT WORKFLOW STATE ==="
    echo "Object Type: $(get_target_object)"
    echo "Scenario: $(cat .process_state/scenario 2>/dev/null || echo 'unknown')"
    echo "Current Phase: $(get_current_phase)"
    echo "Micro-Cycle Count: $(cat .process_state/micro_cycle_count 2>/dev/null || echo '0')"
    
    echo ""
    echo "=== COMPLETED CHECKPOINTS ==="
    if ls .checkpoints/*.* >/dev/null 2>&1; then
        ls .checkpoints/ | sed 's/^/  ✓ /'
    else
        echo "  (none)"
    fi
    
    echo ""
    echo "=== CURRENT PHASE REQUIREMENTS ==="
    show_phase_requirements "$(get_current_phase)"
}

# Show what's required for current phase
show_phase_requirements() {
    local phase="$1"
    
    case "$phase" in
        "requirements_research")
            echo "  1. WebFetch CREATE $(get_target_object) documentation"
            echo "  2. WebFetch SHOW $(get_target_object)S documentation"  
            echo "  3. WebFetch DESCRIBE $(get_target_object) documentation"
            echo "  4. Create requirements document with 15+ properties"
            echo "  5. Validate requirements completeness"
            echo "  → Creates checkpoint: requirements_complete"
            ;;
        "tdd_object_model")
            echo "  1. Execute TDD micro-cycles for object model"
            echo "  2. Identity cycle (equals/hashCode)"
            echo "  3. Property cycles (one per property from requirements)"
            echo "  4. Framework integration (getName/setName/getSchema)"
            echo "  → Creates checkpoint: object_model_complete"
            ;;
        "snapshot_generator")
            echo "  1. TDD micro-cycles for SnapshotGenerator"
            echo "  2. Basic functionality (supports/getPriority)"
            echo "  3. Database querying (snapshotObject)"
            echo "  4. Property mapping and validation"
            echo "  → Creates checkpoint: snapshot_generator_complete"
            ;;
        "diff_comparator")
            echo "  1. TDD micro-cycles for Comparator"
            echo "  2. Basic functionality (supports/getPriority)"
            echo "  3. Object hashing and comparison"
            echo "  4. Property-by-property diff logic"
            echo "  → Creates checkpoint: diff_comparator_complete"
            ;;
        "integration")
            echo "  1. Service registration in META-INF"
            echo "  2. Integration test execution"
            echo "  3. XSD schema integration"
            echo "  4. Complete workflow validation"
            echo "  → Creates checkpoint: integration_complete"
            ;;
        *)
            echo "  Unknown phase requirements"
            ;;
    esac
}

# Execute micro-cycle with state tracking
execute_micro_cycle() {
    local cycle_name="$1"
    local test_class="$2"
    local test_method="$3"
    
    echo "=== EXECUTING MICRO-CYCLE: $cycle_name ==="
    
    # Increment cycle count
    local current_count=$(cat .process_state/micro_cycle_count 2>/dev/null || echo "0")
    local new_count=$((current_count + 1))
    echo "$new_count" > ".process_state/micro_cycle_count"
    
    # Track cycle start time
    echo "$(date)" > ".process_state/cycle_start"
    
    # RED PHASE
    echo ""
    echo "--- RED PHASE ---"
    echo "Writing failing test for: $test_method"
    
    # Validate red phase (test must fail)
    if validate_red_phase "$test_class" "$test_method"; then
        create_checkpoint "red_phase_${cycle_name}"
    else
        echo "RED PHASE FAILED - stopping micro-cycle"
        return 1
    fi
    
    # GREEN PHASE  
    echo ""
    echo "--- GREEN PHASE ---"
    echo "Writing minimal implementation to pass test"
    
    # User writes implementation here
    echo "Write minimal implementation now..."
    echo "Press Enter when implementation is complete:"
    read -r
    
    # Validate green phase (test must pass)
    if validate_green_phase "$test_class" "$test_method"; then
        create_checkpoint "green_phase_${cycle_name}"
        
        # Enhanced validation: Framework integration compliance after implementation
        local object_type=$(get_target_object)
        echo ""
        echo "🔧 VALIDATING FRAMEWORK INTEGRATION..."
        if validate_framework_integration_compliance "$object_type" && validate_property_patterns "$object_type"; then
            echo "✅ Framework integration maintained"
        else
            echo "❌ Framework integration broken by implementation"
            echo "Fix integration issues before continuing"
            return 1
        fi
    else
        echo "GREEN PHASE FAILED - fix implementation before continuing"
        return 1
    fi
    
    # REFACTOR PHASE
    echo ""
    echo "--- REFACTOR PHASE ---"
    echo "Refactor implementation if needed (optional)"
    echo "Press Enter to validate refactor phase:"
    read -r
    
    # Validate refactor phase (all tests still pass)
    if validate_refactor_phase "$test_class"; then
        create_checkpoint "refactor_phase_${cycle_name}"
        create_checkpoint "cycle_${cycle_name}_complete"
    else
        echo "REFACTOR PHASE FAILED - revert changes"
        return 1
    fi
    
    # Calculate cycle duration
    local start_time=$(cat .process_state/cycle_start)
    local end_time=$(date)
    echo ""
    echo "✅ MICRO-CYCLE COMPLETE: $cycle_name"
    echo "Cycle #$new_count completed"
    echo "Started: $start_time"
    echo "Ended: $end_time"
    
    return 0
}

# Complete phase with validation
complete_phase() {
    local object_type=$(get_target_object)
    local current_phase=$(get_current_phase)
    
    echo "Attempting to complete phase: $current_phase"
    
    case "$current_phase" in
        "requirements_research")
            local req_file="../claude_guide/snowflake_requirements/snapshot_diff_requirements/${object_type}_requirements.md"
            if validate_enhanced_property_requirements "$req_file" "$object_type"; then
                create_checkpoint "requirements_complete"
                echo "✅ Requirements phase completed successfully (enhanced validation)"
                return 0
            else
                echo "❌ Requirements phase validation failed"
                return 1
            fi
            ;;
        "tdd_object_model")
            if validate_object_model "$object_type" && validate_comprehensive_test_coverage "$object_type"; then
                create_checkpoint "object_model_complete"
                echo "✅ Object model phase completed successfully"
                return 0
            else
                echo "❌ Object model phase validation failed"
                return 1
            fi
            ;;
        "snapshot_generator")
            # Validate snapshot generator implementation
            local snapshot_file="src/main/java/liquibase/ext/snowflake/snapshot/${object_type}SnapshotGenerator.java"
            local snapshot_test="src/test/java/liquibase/ext/snowflake/snapshot/${object_type}SnapshotGeneratorTest.java"
            
            if [ -f "$snapshot_file" ] && [ -f "$snapshot_test" ] && mvn test -Dtest="${object_type}SnapshotGeneratorTest" -q; then
                create_checkpoint "snapshot_generator_complete"
                echo "✅ Snapshot generator phase completed successfully"
                return 0
            else
                echo "❌ Snapshot generator phase validation failed"
                return 1
            fi
            ;;
        "diff_comparator")
            # Validate diff comparator implementation
            local comparator_file="src/main/java/liquibase/ext/snowflake/diff/compare/${object_type}Comparator.java"
            local comparator_test="src/test/java/liquibase/ext/snowflake/diff/compare/${object_type}ComparatorTest.java"
            
            if [ -f "$comparator_file" ] && [ -f "$comparator_test" ] && mvn test -Dtest="${object_type}ComparatorTest" -q; then
                create_checkpoint "diff_comparator_complete"
                echo "✅ Diff comparator phase completed successfully"
                return 0
            else
                echo "❌ Diff comparator phase validation failed"
                return 1
            fi
            ;;
        "integration")
            if validate_service_registration "$object_type" && validate_8_microcycle_completion "$object_type" && mvn test -Dtest="*${object_type}*Test*" -q; then
                create_checkpoint "integration_complete"
                echo "✅ Integration phase completed successfully"
                echo ""
                echo "🎉 COMPLETE TDD IMPLEMENTATION FINISHED!"
                show_final_summary
                return 0
            else
                echo "❌ Integration phase validation failed"
                return 1
            fi
            ;;
        *)
            echo "ERROR: Cannot complete unknown phase: $current_phase"
            return 1
            ;;
    esac
}

# Show final implementation summary
show_final_summary() {
    local object_type=$(get_target_object)
    local start_time=$(cat .process_state/workflow_started 2>/dev/null || echo "unknown")
    local cycle_count=$(cat .process_state/micro_cycle_count 2>/dev/null || echo "0")
    
    echo "=== IMPLEMENTATION COMPLETE ==="
    echo "Object: $object_type"
    echo "Started: $start_time"
    echo "Completed: $(date)"
    echo "Total Micro-Cycles: $cycle_count"
    echo ""
    echo "✅ Files Created:"
    echo "  - Object Model: src/main/java/liquibase/ext/snowflake/database/${object_type}.java"
    echo "  - Object Tests: src/test/java/liquibase/ext/snowflake/database/${object_type}Test.java"
    echo "  - Snapshot Generator: src/main/java/liquibase/ext/snowflake/snapshot/${object_type}SnapshotGenerator.java"
    echo "  - Snapshot Tests: src/test/java/liquibase/ext/snowflake/snapshot/${object_type}SnapshotGeneratorTest.java"
    echo "  - Diff Comparator: src/main/java/liquibase/ext/snowflake/diff/compare/${object_type}Comparator.java"
    echo "  - Comparator Tests: src/test/java/liquibase/ext/snowflake/diff/compare/${object_type}ComparatorTest.java"
    echo "  - Requirements: ../claude_guide/snowflake_requirements/snapshot_diff_requirements/${object_type}_requirements.md"
    echo ""
    echo "✅ All Tests Passing"
    echo "✅ Services Registered"  
    echo "✅ TDD Discipline Maintained"
    echo ""
    echo "Ready for production use!"
}

echo "State management functions loaded successfully"