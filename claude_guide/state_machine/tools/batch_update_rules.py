#!/usr/bin/env python3
"""
Batch update rules with confidence tracking
"""

import os
import re

RULES_DIR = "/Users/kevinchappell/Documents/GitHub/liquibase/claude_guide/state_machine/rules/global"

# Rules and their confidence levels
RULE_CONFIDENCE = {
    "BALANCE_MONITORING_RULE.md": ("50%", "new rule, unvalidated"),
    "CONFIDENCE_VELOCITY_RULE.md": ("50%", "new rule, unvalidated"),
    "CONTEXT_SWITCH_LIMIT.md": ("75%", "partially validated through experience"),
    "MOMENTUM_ADAPTATION_RULE.md": ("50%", "new rule, unvalidated"),
    "PERFECTION_TRAP_RULE.md": ("80%", "well-known anti-pattern"),
    "PLANNING_TIME_LIMIT.md": ("70%", "common practice, needs fine-tuning"),
    "PROGRESS_STALL_RULE.md": ("75%", "proven concept, threshold needs validation"),
    "RESEARCH_TIME_LIMIT.md": ("65%", "conceptually sound, needs validation"),
    "TIME_ESTIMATION_RULE.md": ("60%", "formula needs calibration"),
}

def update_rule_file(filepath, confidence, reason):
    """Update a rule file with confidence tracking"""
    
    with open(filepath, 'r') as f:
        content = f.read()
    
    # Skip if already has confidence
    if "## Performance Metrics" in content:
        print(f"  ✓ {os.path.basename(filepath)} already has confidence tracking")
        return
    
    # Find the metadata section and add confidence after Last Updated
    pattern = r'(- \*\*Last Updated\*\*: [^\n]+)\n(- \*\*Validated Through\*\*: [^\n]+)?'
    replacement = f'\\1\n- **Confidence**: {confidence} ({reason})'
    content = re.sub(pattern, replacement, content)
    
    # Add Performance Metrics section after metadata
    pattern = r'(## Purpose)'
    replacement = '''
## Performance Metrics
- **Times Applied**: 0
- **Success Rate**: N/A
- **Last Applied**: Never
- **Average Time Impact**: Unknown

\\1'''
    content = re.sub(pattern, replacement, content)
    
    # Update Metrics section if it exists
    if "## Metrics" in content and "Initial Confidence" in content:
        pattern = r'## Metrics\n- \*\*Initial Confidence\*\*: [^\n]+\n'
        replacement = f'''## Metrics
- **Current Confidence**: {confidence} ({reason})
'''
        content = re.sub(pattern, replacement, content)
        
        # Add effectiveness metrics before Related Documents
        if "## Effectiveness Metrics" not in content:
            pattern = r'(## Related Documents)'
            replacement = '''## Effectiveness Metrics
- **Time Saved**: To be measured
- **Errors Prevented**: To be measured
- **Rework Reduced**: To be measured

## Learning Connections
- **Reinforces**: To be identified
- **Conflicts With**: None identified
- **Depends On**: To be identified
- **Leads To**: To be identified

## Feedback Protocol
- **Success**: +10% confidence (first success), +5% (subsequent)
- **Failure**: -15% confidence
- **Modification**: Reset to 50%
- **Review Triggers**: After 10 uses or monthly

\\1'''
            content = re.sub(pattern, replacement, content)
    
    # Update Learning History to Confidence Evolution
    if "## Learning History" in content:
        pattern = r'## Learning History\n\| Date \| Learning \| Impact \|\n\|------\|----------\|--------\|\n(\| [^\n]+ \| [^\n]+ \| [^\n]+ \|)?'
        
        # Extract existing learning if present
        match = re.search(pattern, content)
        existing_row = ""
        if match and match.group(1):
            # Parse existing row
            parts = match.group(1).split("|")
            if len(parts) >= 4:
                date = parts[1].strip()
                learning = parts[2].strip()
                existing_row = f'\n| {date} | Initial validation | 50% | {confidence} | {learning} |'
        
        replacement = f'''## Confidence Evolution
| Date | Event | Old Conf | New Conf | Evidence |
|------|-------|----------|----------|----------|
| 2025-01-26 | Created | 0% | 50% | New rule from LBCF |{existing_row}'''
        
        content = re.sub(pattern, replacement, content, flags=re.DOTALL)
    
    # Write updated content
    with open(filepath, 'w') as f:
        f.write(content)
    
    print(f"  ✓ Updated {os.path.basename(filepath)}")

# Process all rules
for rule_file, (confidence, reason) in RULE_CONFIDENCE.items():
    filepath = os.path.join(RULES_DIR, rule_file)
    if os.path.exists(filepath):
        update_rule_file(filepath, confidence, reason)
    else:
        print(f"  ✗ {rule_file} not found")

print("\nDone updating rules!")