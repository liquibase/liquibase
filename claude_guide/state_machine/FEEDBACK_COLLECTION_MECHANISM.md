# Feedback Collection Mechanism

## Overview
Systematic approach to collecting outcome data and updating confidence scores for continuous learning.

## Feedback Collection Points

### 1. Real-Time During Work
```markdown
## Rule/Process Applied: [Name]
**Time**: [Timestamp]
**Context**: [What you were doing]
**Outcome**: ✅ Success / ⚠️ Partial / ❌ Failure
**Time Impact**: Saved [X] min / Lost [Y] min
**Notes**: [What happened]
```

### 2. End of Task
```markdown
## Task Completion Feedback
**Rules Applied**: 
- THREE_STRIKE_META_RULE: ✅ Prevented infinite loop
- CONFIDENCE_VELOCITY: ⚠️ Estimate was 20% off

**Processes Used**:
- DEVELOPMENT_CYCLE: ✅ All phases completed smoothly
- PATTERN_MATCHING: ❌ No relevant patterns found
```

### 3. Daily Retrospective
```markdown
## Daily Feedback Summary - [Date]
**Most Helpful**: ITERATION_WITHOUT_PROGRESS (saved 2 hours)
**Least Helpful**: MOMENTUM_ADAPTATION (not applicable)
**Missing**: Need rule for dependency management
**Conflicts**: PERFECTION_TRAP vs quality requirements
```

## Feedback Templates

### Success Feedback
```markdown
## SUCCESS: [Rule/Process Name]
**Confidence Update**: +10% (first success) or +5% (repeat)
**Evidence**: [What specifically worked]
**Time Saved**: [Actual measurement]
**Pattern Identified**: [Reusable insight]
**Recommendation**: [How to improve further]
```

### Failure Feedback
```markdown
## FAILURE: [Rule/Process Name]
**Confidence Update**: -15%
**Root Cause**: [Why it failed]
**Context**: [Special circumstances]
**Attempted Fix**: [What you tried]
**Recommendation**: [How to fix the rule/process]
```

### Modification Feedback
```markdown
## MODIFIED: [Rule/Process Name]
**Confidence Update**: -5% (needs recalibration)
**What Changed**: [Specific modifications]
**Why Changed**: [Reason for modification]
**New Version**: [Updated rule/process]
**Testing Needed**: [Validation requirements]
```

## Automated Collection Scripts

### Git Hook for Confidence Updates
```bash
#!/bin/bash
# post-commit hook to prompt for feedback

echo "Did you apply any rules/processes? (y/n)"
read response
if [ "$response" = "y" ]; then
    echo "Which ones? (comma separated)"
    read items
    echo "Outcomes? (success/partial/failure)"
    read outcomes
    # Log to feedback file
    echo "$(date): $items - $outcomes" >> state_machine/feedback/feedback.log
fi
```

### Weekly Confidence Updater
```python
# update_confidence.py
def update_confidence(item, outcome, current_conf):
    if outcome == "success":
        if item.times_applied == 0:
            new_conf = min(98, current_conf + 10)
        else:
            new_conf = min(98, current_conf + 5)
    elif outcome == "failure":
        new_conf = max(10, current_conf - 15)
    else:  # partial
        new_conf = current_conf + 2
    
    return new_conf
```

## Feedback Analysis Process

### 1. Collect Raw Feedback
- Git commits mentioning rules/processes
- Task completion notes
- Time tracking data
- Error logs

### 2. Categorize Outcomes
- Full Success: Worked as designed
- Partial Success: Worked with modifications
- Failure: Didn't help or made worse
- Not Applicable: Wrong context

### 3. Calculate Confidence Updates
```
New Confidence = Current + Adjustment
Where Adjustment =
- First Success: +10%
- Repeat Success: +5% (diminishing returns)
- Partial Success: +2%
- Failure: -15%
- Major Failure: -20%
- Time Decay: -5% per unused month
```

### 4. Update Documentation
- Edit confidence in rule/process metadata
- Add row to Confidence Evolution table
- Update dashboard
- Note in change log

## Integration Points

### With State Machine
- Log rule applications during state transitions
- Track confidence at decision points
- Flag low-confidence paths

### With Development Tools
- IDE plugins to mark rule usage
- Git aliases for feedback commits
- CI/CD confidence reporting

### With Time Tracking
- Correlate time saved/lost with rules
- Identify high-impact items
- Priority validation targets

## Feedback Loops

### Immediate (Real-time)
- Pop-up after rule trigger
- Quick thumbs up/down
- One-click time impact

### Short-term (Daily)
- End-of-day summary
- Batch confidence updates
- Pattern identification

### Long-term (Weekly/Monthly)
- Trend analysis
- Systemic improvements
- Rule/process evolution

## Success Metrics
- 80%+ feedback capture rate
- <24 hour feedback lag
- 95% confidence accuracy
- Continuous improvement trend

## Next Steps
1. Create feedback.log file structure
2. Build confidence update scripts
3. Add feedback prompts to workflows
4. Create weekly analysis reports