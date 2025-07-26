#!/usr/bin/env python3
"""
Batch update processes with confidence tracking
"""

import os
import re

PROCESSES_DIR = "/Users/kevinchappell/Documents/GitHub/liquibase/claude_guide/state_machine/processes"

# Processes and their confidence levels
PROCESS_CONFIDENCE = {
    "CELEBRATION_PROTOCOL_PROCESS.md": ("50%", "new process, unvalidated"),
    "CONTINUOUS_IMPROVEMENT_PROCESS.md": ("75%", "common practice, needs calibration"),
    "DEVELOPMENT_CYCLE.md": ("85%", "validated through use, proven 5-phase cycle"),
    "DOCUMENT_STANDARDS_EVOLUTION.md": ("70%", "working well, needs refinement"),
    "FAILURE_ANALYSIS_PROCESS.md": ("80%", "well-established practice"),
    "MISTAKE_TO_PATTERN_PROCESS.md": ("60%", "good concept, needs validation"),
    "MULTI_PERSPECTIVE_REVIEW.md": ("65%", "useful but needs streamlining"),
    "PATTERN_MATCHING_PROCESS.md": ("55%", "early stage, needs data"),
    "RISK_ASSESSMENT_PROCESS.md": ("70%", "standard practice"),
    "SUCCESS_AMPLIFIER_PROCESS.md": ("50%", "new process, unvalidated"),
    "SUCCESS_CAPTURE_PROCESS.md": ("75%", "proven valuable"),
}

def update_process_file(filepath, confidence, reason):
    """Update a process file with confidence tracking"""
    
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
- **Quality Improved**: To be measured
- **Errors Prevented**: To be measured

## Learning Connections
- **Reinforces**: To be identified
- **Conflicts With**: None identified
- **Depends On**: To be identified
- **Enables**: To be identified

## Feedback Protocol
- **Success**: +10% confidence (process worked well)
- **Failure**: -15% confidence (process failed)
- **Modification**: -5% confidence (needed changes)
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
                existing_row = f'\n| {date} | Initial use | 50% | {confidence} | {learning} |'
        
        replacement = f'''## Confidence Evolution
| Date | Event | Old Conf | New Conf | Evidence |
|------|-------|----------|----------|----------|
| 2025-01-26 | Created | 0% | 50% | New process from LBCF |{existing_row}'''
        
        content = re.sub(pattern, replacement, content, flags=re.DOTALL)
    
    # Write updated content
    with open(filepath, 'w') as f:
        f.write(content)
    
    print(f"  ✓ Updated {os.path.basename(filepath)}")

# Process all processes
for process_file, (confidence, reason) in PROCESS_CONFIDENCE.items():
    filepath = os.path.join(PROCESSES_DIR, process_file)
    if os.path.exists(filepath):
        update_process_file(filepath, confidence, reason)
    else:
        print(f"  ✗ {process_file} not found")

print("\nDone updating processes!")