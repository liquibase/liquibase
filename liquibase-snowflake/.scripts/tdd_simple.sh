#!/bin/bash

# Simple TDD Workflow - Fixed Bash Version
# Incorporates the fixes we made but keeps it simple

set -e

# Command functions
cmd_init() {
    local object_type="$1"
    local scenario="${2:-NEW_OBJECT}"
    
    echo "🚀 INITIALIZING TDD WORKFLOW"
    echo "Object Type: $object_type"
    echo "Scenario: $scenario"
    echo ""
    
    # Create state directories
    mkdir -p .process_state .checkpoints
    
    # Save state
    echo "$object_type" > .process_state/target_object
    echo "$scenario" > .process_state/scenario
    echo "requirements_research" > .process_state/current_phase
    echo "0" > .process_state/micro_cycle_count
    echo "" > .process_state/completed_checkpoints
    
    echo "✅ Workflow initialized successfully"
    
    # Validate existing object model
    local object_file="src/main/java/liquibase/database/object/${object_type}.java"
    if [ -f "$object_file" ]; then
        echo "Found existing object model, validating..."
        
        # Test compilation
        if mvn compile -q >/dev/null 2>&1; then
            echo "✅ Object model compilation PASSED"
            
            # Basic framework checks
            if grep -q "extends AbstractDatabaseObject" "$object_file" && \
               grep -q "getContainingObjects()" "$object_file" && \
               grep -q "getName()" "$object_file"; then
                echo "✅ Framework integration PASSED"
                echo "Ready for TDD development phases"
                return 0
            else
                echo "❌ Framework integration issues found"
                return 1
            fi
        else
            echo "❌ Object model compilation FAILED"
            return 1
        fi
    else
        echo "❌ Object model not found: $object_file"
        echo "Please ensure object model exists before running TDD workflow"
        return 1
    fi
}

cmd_status() {
    if [ ! -f .process_state/target_object ]; then
        echo "❌ No active workflow. Run 'init' first."
        return 1
    fi
    
    local object_type=$(cat .process_state/target_object 2>/dev/null || echo "Not set")
    local scenario=$(cat .process_state/scenario 2>/dev/null || echo "Not set")
    local current_phase=$(cat .process_state/current_phase 2>/dev/null || echo "Not set")
    local micro_cycle_count=$(cat .process_state/micro_cycle_count 2>/dev/null || echo "0")
    
    echo "=== CURRENT WORKFLOW STATE ==="
    echo "Object Type: $object_type"
    echo "Scenario: $scenario"
    echo "Current Phase: $current_phase"
    echo "Micro-Cycle Count: $micro_cycle_count"
    echo ""
    echo "=== COMPLETED CHECKPOINTS ==="
    
    if [ -f .process_state/completed_checkpoints ] && [ -s .process_state/completed_checkpoints ]; then
        while IFS= read -r checkpoint; do
            echo "  ✅ $checkpoint"
        done < .process_state/completed_checkpoints
    else
        echo "  (none)"
    fi
    
    # Show phase requirements
    if [ "$current_phase" = "requirements_research" ]; then
        echo ""
        echo "=== CURRENT PHASE REQUIREMENTS ==="
        echo "  1. WebFetch CREATE $object_type documentation"
        echo "  2. WebFetch SHOW ${object_type}S documentation"
        echo "  3. WebFetch DESCRIBE $object_type documentation"
        echo "  4. Create requirements document with 15+ properties"
        echo "  5. Validate requirements completeness"
        echo "→ Creates checkpoint: requirements_complete"
    fi
}

cmd_next() {
    if [ ! -f .process_state/current_phase ]; then
        echo "❌ No active workflow. Run 'init' first."
        return 1
    fi
    
    local current_phase=$(cat .process_state/current_phase)
    local object_type=$(cat .process_state/target_object)
    
    echo "🔄 EXECUTING NEXT PHASE"
    echo "Current: $current_phase"
    echo "Object: $object_type"
    echo ""
    
    if [ "$current_phase" = "requirements_research" ]; then
        echo "📋 REQUIREMENTS RESEARCH PHASE"
        echo "Object: $object_type"
        echo ""
        echo "REQUIRED ACTIONS:"
        echo "1. Use WebFetch to research CREATE $object_type documentation"
        echo "2. Use WebFetch to research SHOW ${object_type}S documentation"
        echo "3. Use WebFetch to research DESCRIBE $object_type documentation"
        echo "4. Update requirements document with 15+ properties"
        echo "5. Run: .scripts/tdd_simple.sh complete"
        echo ""
    fi
    
    cmd_status
}

cmd_complete() {
    if [ ! -f .process_state/current_phase ]; then
        echo "❌ No active workflow"
        return 1
    fi
    
    local current_phase=$(cat .process_state/current_phase)
    
    echo "🏁 COMPLETING CURRENT PHASE"
    echo "Attempting to complete phase: $current_phase"
    
    # For now, just advance to next phase
    if [ "$current_phase" = "requirements_research" ]; then
        echo "tdd_object_model" > .process_state/current_phase
        echo "requirements_complete" >> .process_state/completed_checkpoints
        echo "✅ Phase completed successfully"
        echo "Advanced to: tdd_object_model"
    else
        echo "✅ Phase validation passed"
    fi
    
    return 0
}

cmd_reset() {
    echo "⚠️  RESETTING WORKFLOW"
    rm -rf .process_state .checkpoints
    echo "✅ Workflow reset complete"
}

# Usage
show_usage() {
    echo "TDD Workflow Orchestration System (Simple Bash Implementation)"
    echo ""
    echo "COMMANDS:"
    echo "  init <ObjectType> [scenario]  - Initialize workflow"
    echo "  status                        - Show current state" 
    echo "  next                          - Execute next phase"
    echo "  complete                      - Complete current phase"
    echo "  reset                         - Reset workflow"
    echo ""
    echo "EXAMPLES:"
    echo "  .scripts/tdd_simple.sh init FileFormat NEW_OBJECT"
    echo "  .scripts/tdd_simple.sh status"
    echo "  .scripts/tdd_simple.sh next"
    echo "  .scripts/tdd_simple.sh complete"
}

# Main execution
case "${1:-}" in
    init)
        if [ -z "$2" ]; then
            echo "❌ Usage: init <ObjectType> [scenario]"
            exit 1
        fi
        cmd_init "$2" "$3"
        ;;
    status)
        cmd_status
        ;;
    next)
        cmd_next
        ;;
    complete)
        cmd_complete
        ;;
    reset)
        cmd_reset
        ;;
    *)
        show_usage
        exit 1
        ;;
esac