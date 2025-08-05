#!/bin/bash
# Main TDD workflow orchestration script
# Enforces complete state-driven TDD discipline

set -e
source .scripts/validation_functions.sh
source .scripts/state_management.sh

# Main workflow entry point
main() {
    local command="${1:-help}"
    
    case "$command" in
        "init")
            initialize_object_implementation "$2" "$3"
            ;;
        "status")
            show_current_state
            ;;
        "next")
            execute_next_phase
            ;;
        "cycle")
            execute_tdd_cycle "$2" "$3" "$4"
            ;;
        "complete")
            complete_current_phase
            ;;
        "reset")
            reset_workflow
            ;;
        *)
            show_help
            ;;
    esac
}

# Initialize complete object implementation workflow
initialize_object_implementation() {
    local object_type="$1"
    local scenario="${2:-NEW_OBJECT}"
    
    if [ -z "$object_type" ]; then
        echo "ERROR: Object type required"
        echo "Usage: $0 init <ObjectType> [scenario]"
        echo "Example: $0 init FileFormat NEW_OBJECT"
        exit 1
    fi
    
    echo "🚀 INITIALIZING TDD WORKFLOW"
    echo "Object Type: $object_type"
    echo "Scenario: $scenario"
    echo ""
    
    # Initialize workflow state
    initialize_workflow "$object_type" "$scenario"
    
    # Generate template files if NEW_OBJECT scenario
    if [ "$scenario" = "NEW_OBJECT" ]; then
        echo "Generating template files..."
        .scripts/template_substitution.sh generate "$object_type"
        
        echo ""
        echo "🔧 VALIDATING TEMPLATE GENERATION..."
        validate_complete_template_generation "$object_type" || {
            echo "❌ Template generation validation failed"
            echo "Fix template issues before proceeding"
            return 1
        }
        echo ""
    fi
    
    echo "✅ TDD workflow initialized successfully"
    echo ""
    echo "Next steps:"
    echo "  $0 status  - Show current state"
    echo "  $0 next    - Execute next phase"
}

# Execute next phase in workflow
execute_next_phase() {
    local current_phase=$(get_current_phase)
    local object_type=$(get_target_object)
    
    echo "🔄 EXECUTING NEXT PHASE"
    echo "Current: $current_phase"
    echo "Object: $object_type"
    echo ""
    
    case "$current_phase" in
        "infrastructure_setup")
            echo "Infrastructure setup already complete"
            transition_to_phase "requirements_research"
            show_current_state
            ;;
        "requirements_research")
            execute_requirements_phase
            ;;
        "tdd_object_model")
            execute_object_model_phase
            ;;
        "snapshot_generator")
            execute_snapshot_generator_phase
            ;;
        "diff_comparator")
            execute_diff_comparator_phase
            ;;
        "integration")
            execute_integration_phase
            ;;
        *)
            echo "ERROR: Unknown phase: $current_phase"
            return 1
            ;;
    esac
}

# Execute requirements research phase
execute_requirements_phase() {
    local object_type=$(get_target_object)
    
    echo "📋 REQUIREMENTS RESEARCH PHASE"
    echo "Object: $object_type"
    echo ""
    
    echo "REQUIRED ACTIONS:"
    echo "1. Use WebFetch to research CREATE $object_type documentation"
    echo "2. Use WebFetch to research SHOW ${object_type}S documentation"
    echo "3. Use WebFetch to research DESCRIBE $object_type documentation"
    echo "4. Update requirements document with 15+ properties"
    echo "5. Run: $0 complete"
    echo ""
    
    show_phase_requirements "requirements_research"
}

# Execute object model TDD phase
execute_object_model_phase() {
    local object_type=$(get_target_object)
    
    echo "🏗️ TDD OBJECT MODEL PHASE"
    echo "Object: $object_type"
    echo ""
    
    echo "REQUIRED TDD MICRO-CYCLES:"
    echo "1. Identity cycle: equals/hashCode implementation"
    echo "2. Property cycles: Add properties from requirements (one per cycle)"
    echo "3. Framework integration: getName/setName/getSchema methods"
    echo ""
    echo "Use: $0 cycle <cycle_name> <test_class> <test_method>"
    echo "Example: $0 cycle identity_check ${object_type}Test testEqualsContract"
    echo ""
    
    show_phase_requirements "tdd_object_model"
}

# Execute snapshot generator TDD phase
execute_snapshot_generator_phase() {
    local object_type=$(get_target_object)
    
    echo "📸 TDD SNAPSHOT GENERATOR PHASE"
    echo "Object: $object_type"
    echo ""
    
    echo "REQUIRED TDD MICRO-CYCLES:"
    echo "1. Basic functionality: supports/getPriority methods"
    echo "2. Database querying: snapshotObject implementation"
    echo "3. Property mapping: Map query results to object properties"
    echo "4. Validation: Error handling and edge cases"
    echo ""
    echo "Use: $0 cycle <cycle_name> <test_class> <test_method>"
    echo "Example: $0 cycle basic_support ${object_type}SnapshotGeneratorTest testSupportsCorrectType"
    echo ""
    
    show_phase_requirements "snapshot_generator"
}

# Execute diff comparator TDD phase
execute_diff_comparator_phase() {
    local object_type=$(get_target_object)
    
    echo "🔍 TDD DIFF COMPARATOR PHASE"
    echo "Object: $object_type"
    echo ""
    
    echo "REQUIRED TDD MICRO-CYCLES:"
    echo "1. Basic functionality: supports/getPriority methods"
    echo "2. Object hashing: isSameObject implementation"
    echo "3. Property comparison: compareObjects with property-by-property logic"
    echo "4. Difference detection: ObjectDifferences handling"
    echo ""
    echo "Use: $0 cycle <cycle_name> <test_class> <test_method>"
    echo "Example: $0 cycle basic_support ${object_type}ComparatorTest testSupportsCorrectType"
    echo ""
    
    show_phase_requirements "diff_comparator"
}

# Execute integration phase
execute_integration_phase() {
    local object_type=$(get_target_object)
    
    echo "🔗 INTEGRATION PHASE"
    echo "Object: $object_type"
    echo ""
    
    echo "REQUIRED ACTIONS:"
    echo "1. Register ${object_type}SnapshotGenerator in META-INF services"
    echo "2. Register ${object_type}Comparator in META-INF services"
    echo "3. Run integration tests with Snowflake database"
    echo "4. Validate XSD schema if needed"
    echo "5. Run: $0 complete"
    echo ""
    
    show_phase_requirements "integration"
}

# Execute TDD micro-cycle
execute_tdd_cycle() {
    local cycle_name="$1"
    local test_class="$2"
    local test_method="$3"
    
    if [ -z "$cycle_name" ] || [ -z "$test_class" ] || [ -z "$test_method" ]; then
        echo "ERROR: All parameters required for TDD cycle"
        echo "Usage: $0 cycle <cycle_name> <test_class> <test_method>"
        echo "Example: $0 cycle identity_check FileFormatTest testEqualsContract"
        return 1
    fi
    
    execute_micro_cycle "$cycle_name" "$test_class" "$test_method"
}

# Complete current phase
complete_current_phase() {
    echo "🏁 COMPLETING CURRENT PHASE"
    echo ""
    
    if complete_phase; then
        # Automatically transition to next phase
        local current_phase=$(get_current_phase)
        local next_phase
        
        case "$current_phase" in
            "requirements_research")
                next_phase="tdd_object_model"
                ;;
            "tdd_object_model")
                next_phase="snapshot_generator"
                ;;
            "snapshot_generator")
                next_phase="diff_comparator"
                ;;
            "diff_comparator")
                next_phase="integration"
                ;;
            "integration")
                echo "🎉 COMPLETE IMPLEMENTATION FINISHED!"
                return 0
                ;;
        esac
        
        if [ -n "$next_phase" ]; then
            echo "Transitioning to next phase: $next_phase"
            transition_to_phase "$next_phase"
            echo ""
            show_current_state
        fi
    else
        echo "❌ Phase completion failed - check requirements"
        return 1
    fi
}

# Reset workflow (for testing/debugging)
reset_workflow() {
    echo "⚠️  RESETTING WORKFLOW"
    echo "This will clear all progress and checkpoints."
    echo -n "Are you sure? (y/N): "
    read -r confirmation
    
    if [ "$confirmation" = "y" ] || [ "$confirmation" = "Y" ]; then
        rm -rf .process_state .checkpoints
        mkdir -p .process_state .checkpoints
        echo "✅ Workflow reset complete"
    else
        echo "Reset cancelled"
    fi
}

# Show help
show_help() {
    echo "TDD Workflow Orchestration System"
    echo ""
    echo "COMMANDS:"
    echo "  init <ObjectType> [scenario]  - Initialize workflow for object type"
    echo "  status                        - Show current workflow state"
    echo "  next                          - Execute next phase"
    echo "  cycle <name> <class> <method> - Execute TDD micro-cycle"
    echo "  complete                      - Complete current phase"
    echo "  reset                         - Reset workflow (clears all progress)"
    echo ""
    echo "EXAMPLES:"
    echo "  $0 init FileFormat NEW_OBJECT"
    echo "  $0 status"
    echo "  $0 next"
    echo "  $0 cycle identity_check FileFormatTest testEqualsContract"
    echo "  $0 complete"
    echo ""
    echo "WORKFLOW PHASES:"
    echo "  1. requirements_research  - WebFetch documentation and create requirements"
    echo "  2. tdd_object_model      - TDD micro-cycles for object model"
    echo "  3. snapshot_generator    - TDD micro-cycles for snapshot generator"
    echo "  4. diff_comparator       - TDD micro-cycles for diff comparator"
    echo "  5. integration           - Service registration and integration tests"
    echo ""
    echo "SCENARIOS:"
    echo "  NEW_OBJECT              - Complete new object implementation"
    echo "  ENHANCE_EXISTING        - Add properties to existing object"
    echo "  COMPLETE_INCOMPLETE     - Finish partial implementation"
    echo "  FIX_BUGS               - Systematic bug fixing"
    echo "  OPTIMIZE_PERFORMANCE   - Performance improvements"
}

# Execute main function
main "$@"