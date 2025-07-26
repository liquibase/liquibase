#!/bin/bash
# Script to add confidence tracking to all rules

RULES_DIR="/Users/kevinchappell/Documents/GitHub/liquibase/claude_guide/state_machine/rules/global"

# Function to check if file already has confidence tracking
has_confidence() {
    grep -q "## Performance Metrics" "$1"
}

# Function to add confidence tracking to a rule
add_confidence() {
    local file=$1
    local rule_name=$(basename "$file" .md)
    
    echo "Processing $rule_name..."
    
    # Skip if already has confidence
    if has_confidence "$file"; then
        echo "  ✓ Already has confidence tracking"
        return
    fi
    
    # Check which confidence level this rule should have
    local confidence="50%"
    local reason="new rule, unvalidated"
    
    # Special cases for validated rules
    if [[ "$rule_name" == "THREE_STRIKE_META_RULE" ]]; then
        confidence="95%"
        reason="proven loop breaker, validated through experience"
    elif [[ "$rule_name" == "CONFIDENCE_THRESHOLDS" ]]; then
        confidence="90%"
        reason="validated through state machine usage"
    fi
    
    # Create temporary file with updates
    local temp_file="${file}.tmp"
    
    # Process the file
    awk -v conf="$confidence" -v reason="$reason" '
    BEGIN { metadata_done = 0; metrics_added = 0 }
    
    # After Last Updated line in metadata, add confidence
    /^- \*\*Last Updated\*\*:/ && !metadata_done {
        print $0
        print "- **Confidence**: " conf " (" reason ")"
        metadata_done = 1
        next
    }
    
    # After metadata section, add performance metrics
    /^## Purpose/ && !metrics_added {
        print ""
        print "## Performance Metrics"
        print "- **Times Applied**: 0"
        print "- **Success Rate**: N/A"
        print "- **Last Applied**: Never"
        print "- **Average Time Impact**: Unknown"
        print ""
        metrics_added = 1
    }
    
    # Print all other lines
    { print }
    ' "$file" > "$temp_file"
    
    # Replace original file
    mv "$temp_file" "$file"
    echo "  ✓ Added confidence tracking"
}

# Process all rule files
for rule_file in "$RULES_DIR"/*.md; do
    # Skip templates and application guides
    if [[ $(basename "$rule_file") == *"TEMPLATE"* ]] || [[ $(basename "$rule_file") == *"APPLICATION"* ]]; then
        echo "Skipping $(basename "$rule_file")"
        continue
    fi
    
    add_confidence "$rule_file"
done

echo "Done processing rules!"